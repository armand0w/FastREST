package com.armandow.rest;

import com.armandow.rest.annotatios.*;
import com.armandow.rest.enums.ContentParamType;
import com.armandow.rest.enums.ContentType;
import com.armandow.rest.enums.MethodType;
import com.armandow.rest.exceptions.*;
import com.armandow.rest.model.Execution;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

@Slf4j
@WebServlet(value = "/*", name = "api-rest-application")
public class RESTApplication extends HttpServlet {
    @Serial
    private static final long serialVersionUID = 478998679743033075L;
    private static final Map<String, MethodType> methodTypeMap;
    private static final Map<String, ContentType> contentTypeMap;
    private transient List<RESTMethodReference> methodReferenceList;

    static {
        contentTypeMap = new HashMap<>();
        methodTypeMap = new HashMap<>();

        for ( ContentType ct: ContentType.values() )
            contentTypeMap.put(ct.toString(), ct);

        for ( MethodType mt: MethodType.values() )
            methodTypeMap.put(mt.name(), mt);
    }

    private void getAllClasses(ServletContext servletContext, String path) {
        var resourcePaths = servletContext.getResourcePaths(path);

        if ( resourcePaths != null ) {
            for ( var resourcePath: resourcePaths ) {
                var file = new File(servletContext.getRealPath("/") + resourcePath);

                if ( file.isDirectory() ) {
                    getAllClasses(servletContext, resourcePath);
                } else {
                    if ( file.isFile() && resourcePath.endsWith(".class") ) {
                        var auxPath = resourcePath.substring(0, resourcePath.indexOf("/", 9));
                        var className = resourcePath.replace(".class", "").replace(auxPath + "/", "").replace("/", ".");

                        try {
                            var annotations = Class.forName(className).getAnnotations();

                            for ( var annotation: annotations ) {
                                if ( annotation instanceof RESTController rc ) {
                                    var controllerPath = rc.value();
                                    var methods = Class.forName(className).getMethods();

                                    for ( var method: methods ) {
                                        var methodAnnotations = method.getAnnotations();

                                        for ( var methodAnnotation: methodAnnotations ) {
                                            Annotation[] arrayAnnotations = null;

                                            if ( methodAnnotation instanceof RESTMethod ) {
                                                arrayAnnotations = new Annotation[1];
                                                arrayAnnotations[0] = methodAnnotation;
                                            }

                                            if ( methodAnnotation instanceof RESTMethods rm ) {
                                                arrayAnnotations = rm.value();
                                            }

                                            if ( arrayAnnotations != null ) {
                                                for ( var a: arrayAnnotations ) {
                                                    var methodPath = ((RESTMethod) a).path();
                                                    var contentType = ((RESTMethod) a).contentType();
                                                    var produces = ((RESTMethod) a).produces();
                                                    var producesCharset = ((RESTMethod) a).producesCharset();
                                                    var methodType = ((RESTMethod) a).method();

                                                    var restMethodReference = new RESTMethodReference();
                                                    restMethodReference.setClassName(className);
                                                    restMethodReference.setMethod(method.getName());
                                                    restMethodReference.setPath(controllerPath + methodPath);
                                                    restMethodReference.setMethodType(methodType);
                                                    restMethodReference.setContentType(contentType);
                                                    restMethodReference.setProduces(produces);
                                                    restMethodReference.setProducesCharset(producesCharset);

                                                    methodReferenceList.add(restMethodReference);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.error("getAllClasses", e);
                        }
                    }
                }
            }
        }
    }

    private String getDynamicPath(String annotationPath, String urlPath, Map<String, String> pathParamMap) {
        var annotationArrayPath = annotationPath.split("/");
        var urlArrayPath = urlPath.split("/");
        var stringBuffer = new StringBuffer();
        var result = annotationPath;

        if ( annotationArrayPath.length == urlArrayPath.length ) {
            for ( int i = 0; i < annotationArrayPath.length; i++ ) {
                var annotationElement = annotationArrayPath[i];

                if ( annotationElement.trim().isEmpty() ) {
                    continue;
                }

                if ( annotationElement.startsWith("{") && annotationElement.endsWith("}") ) {
                    stringBuffer.append("/").append(urlArrayPath[i]);
                    pathParamMap.put(annotationElement.substring(1, annotationElement.length() - 1), urlArrayPath[i]);
                } else {
                    stringBuffer.append("/").append(annotationElement);
                }
            }

            result = stringBuffer.toString();
        }

        return result;
    }

    private void findMethod(String path, MethodType methodType, ContentType contentType, HashMap<String, String> pathParamMap, Execution execution) throws OperationNotFoundException {
        execution.setExecutionMethod(null);
        execution.setExecutionObject(null);
        execution.setExecutionProduces(null);
        execution.setExecutionProducesCharset(null);

        if ( methodType != null && path != null ) {
            try {
                for ( var restMethodReference: methodReferenceList ) {
                    var dynamicPath = getDynamicPath(restMethodReference.getPath(), path, pathParamMap);

                    if ( restMethodReference.getMethodType() == methodType && dynamicPath.equals(path) && restMethodReference.getContentType() == contentType ) {
                        var clazz = Class.forName(restMethodReference.getClassName());
                        var methods = clazz.getMethods();

                        execution.setExecutionObject(clazz.getDeclaredConstructor().newInstance());

                        for ( var method: methods ) {
                            if ( method.getName().equals(restMethodReference.getMethod()) ) {
                                execution.setExecutionMethod(method);
                                execution.setExecutionProduces(restMethodReference.getProduces());
                                execution.setExecutionProducesCharset(restMethodReference.getProducesCharset());
                                return;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("findMethod", e);
            }
        }

        throw new OperationNotFoundException("Operation not allowed");
    }

    private Object executeMethod(HttpServletRequest servletRequest, String content, HashMap<String, String> parameters, HashMap<String, String> headers, HashMap<String, String> pathParamMap, String contentType, Execution execution) throws AuthorizationException, OperationNotFoundException, ContentException, OperationExecutionException, CustomException {
        if ( execution.getExecutionMethod() == null || execution.getExecutionObject() == null || execution.getExecutionProduces() == null ) {
            throw new OperationNotFoundException("Unsupported operation");
        }

        var restMethodParameters = execution.getExecutionMethod().getParameters();
        Object[] parametersRestMethod = null;
        var ip = 0;

        if ( restMethodParameters != null && restMethodParameters.length > 0 ) {
            parametersRestMethod = new Object[restMethodParameters.length];

            for ( var restMethodParameter: restMethodParameters ) {
                parametersRestMethod[ip] = null;

                var restMethodParameterAnnotations = restMethodParameter.getAnnotations();

                for ( var restMethodParameterAnnotation: restMethodParameterAnnotations ) {
                    if ( restMethodParameterAnnotation instanceof RESTContentParam rcp ) {
                        if (content == null) {
                            parametersRestMethod[ip] = null;
                        } else {
                            if (rcp.value() == ContentParamType.JSON) {
                                try {
                                    parametersRestMethod[ip] = new JSONObject(content);
                                } catch (Exception e) {
                                    throw new ContentException(e.getMessage());
                                }
                            }
                            if (rcp.value() == ContentParamType.TEXT) {
                                parametersRestMethod[ip] = content;
                            }
                        }
                    }
                    if ( restMethodParameterAnnotation instanceof RESTHeaderParam rhp ) {
                        parametersRestMethod[ip] = headers.get(rhp.value().toLowerCase());
                    }
                    if ( restMethodParameterAnnotation instanceof RESTURLParam rup ) {
                        parametersRestMethod[ip] = parameters.get(rup.value().toLowerCase());
                    }
                    if ( restMethodParameterAnnotation instanceof RESTPathParam rpp ) {
                        parametersRestMethod[ip] = pathParamMap.get(rpp.value());
                    }
                    if ( restMethodParameterAnnotation instanceof RESTServerName ) {
                        parametersRestMethod[ip] = servletRequest.getServerName();
                    }
                    if ( restMethodParameterAnnotation instanceof RESTServerPort ) {
                        parametersRestMethod[ip] = Integer.toString(servletRequest.getServerPort());
                    }
                    if ( restMethodParameterAnnotation instanceof RESTContext ) {
                        parametersRestMethod[ip] = servletRequest.getContextPath();
                    }
                }

                ip++;
            }
        }

        try {
            return execution.getExecutionMethod().invoke(execution.getExecutionObject(), parametersRestMethod);
        } catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
            if ( e instanceof InvocationTargetException ite ) {
                if ( ite.getTargetException() != null ) {
                    var throwable = ite.getTargetException();
                    try {
                        throw new OperationExecutionException(throwable.getMessage());
                    } catch (Exception e1) {
                        Object data = null;
                        int status = 0;
                        try {
                            data = throwable.getClass().getDeclaredMethod("getData").invoke(throwable);
                            status = (int) throwable.getClass().getDeclaredMethod("getHTTPStatus").invoke(throwable);
                        } catch (Exception e2) {
                            log.error("executeMethod", e2);
                        }

                        if ( ite.getTargetException() instanceof AuthorizationException ) {
                            if ( data == null ) {
                                throw new AuthorizationException(e1.getMessage());
                            } else {
                                throw new AuthorizationException(e1.getMessage(), data);
                            }
                        } else if ( ite.getTargetException() instanceof ContentException ) {
                            if ( data == null ) {
                                throw new ContentException(e1.getMessage());
                            } else {
                                throw new ContentException(e1.getMessage(), data);
                            }
                        } else if ( ite.getTargetException() instanceof OperationNotFoundException ) {
                            if ( data == null ) {
                                throw new OperationNotFoundException(e1.getMessage());
                            } else {
                                throw new OperationNotFoundException(e1.getMessage(), data);
                            }
                        } else if ( ite.getTargetException() instanceof CustomException ) {
                            if ( data == null ) {
                                throw new CustomException(e1.getMessage());
                            } else {
                                throw new CustomException(e1.getMessage(), data, (status >= 100 && status <= 599 ? status : 409));
                            }
                        } else {
                            if ( data == null ) {
                                throw new OperationExecutionException(e1.getMessage());
                            } else {
                                throw new OperationExecutionException(e1.getMessage(), data);
                            }
                        }
                    }
                } else {
                    throw new OperationExecutionException(e.getMessage());
                }
            }
        }

        return null;
    }

    @Override
    protected void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {
        var contentLength = servletRequest.getContentLength();
        var path = servletRequest.getPathInfo();
        var parameters = new HashMap<String, String>();
        var headers = new HashMap<String, String>();
        var pathParamMap = new HashMap<String, String>();
        var execution = new Execution();
        String content = null;

        var characterEncoding = servletRequest.getCharacterEncoding() == null ? "US-ASCII" : servletRequest.getCharacterEncoding();
        var method = servletRequest.getMethod() == null ? "GET" : servletRequest.getMethod();
        var contentType = servletRequest.getContentType() == null ? "text/plain" : servletRequest.getContentType();

        if ( contentType.contains(";") ) {
            contentType = contentType.substring(0, contentType.indexOf(";")).trim();
        }

        if ( contentLength > 0 ) {
            ServletInputStream inputStream = servletRequest.getInputStream();

            int c;
            var i = 0;
            var bytes = new byte[contentLength];

            while ( (c = inputStream.read()) != -1 ) {
                bytes[i++] = (byte) c;
            }

            content = new String(bytes, characterEncoding);
        }

        var parameterNames = servletRequest.getParameterNames();

        while ( parameterNames.hasMoreElements() ) {
            var parameterName = parameterNames.nextElement();
            var parameterValue = servletRequest.getParameter(parameterName);
            parameters.put(parameterName, parameterValue);
        }

        var headerNames = servletRequest.getHeaderNames();

        while ( headerNames.hasMoreElements() ) {
            var headerName = headerNames.nextElement().toLowerCase();
            var headerValue = servletRequest.getHeader(headerName);
            headers.put(headerName, headerValue);
        }

        if ( methodReferenceList == null ) {
            methodReferenceList = new ArrayList<>();
            // For deploy war
            getAllClasses(servletRequest.getServletContext(), "/WEB-INF/classes");
            // Package war:war
            getAllClasses(servletRequest.getServletContext(), "/target/classes");
            // For test
            getAllClasses(servletRequest.getServletContext(), "/target/test-classes");
        }

        try {
            findMethod(path, methodTypeMap.get(method), contentTypeMap.get(contentType), pathParamMap, execution);

            servletResponse.setStatus(200);
            servletResponse.setContentType(execution.getExecutionProduces().toString());
            servletResponse.setCharacterEncoding(execution.getExecutionProducesCharset().toString());

            if ( execution.getExecutionProduces() != null ) {
                switch (execution.getExecutionProduces()) {
                    case IMAGE_JPG, IMAGE_PNG, APPLICATION_OCTET_STREAM, APPLICATION_XML, APPLICATION_PDF, APPLICATION_ZIP -> {
                        var bytes = (byte[]) executeMethod(servletRequest, content, parameters, headers, pathParamMap, contentType, execution);
                        servletResponse.getOutputStream().write(bytes);
                        servletResponse.getOutputStream().flush();
                    }
                    default -> {
                        servletResponse.getWriter().print(executeMethod(servletRequest, content, parameters, headers, pathParamMap, contentType, execution));
                        servletResponse.getWriter().flush();
                    }
                }
            } else {
                servletResponse.getWriter().print(executeMethod(servletRequest, content, parameters, headers, pathParamMap, contentType, execution));
                servletResponse.getWriter().flush();
            }
        } catch (AuthorizationException | OperationExecutionException | OperationNotFoundException | ContentException | CustomException e) {
            servletResponse.setStatus(e.getHTTPStatus());
            if ( execution.getExecutionProduces() != null ) {
                if ( Objects.requireNonNull(execution.getExecutionProduces()) == ContentType.APPLICATION_JSON ) {
                    if ( e.getData() != null ) {
                        servletResponse.getWriter().print(e.getData());
                    } else {
                        servletResponse.getWriter().print(e.getMessage());
                    }
                } else {
                    servletResponse.getWriter().print(e.getMessage());
                }
            } else {
                servletResponse.getWriter().print(e.getMessage());
            }

            servletResponse.getWriter().flush();
        }
    }
}
