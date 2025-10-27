package core.chat.auth;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@RequiredArgsConstructor
public class UserIdArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String CLIENT_ID_HEADER = "X-Client-Id";

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasUserIdAnnotation = parameter.hasParameterAnnotation(UserId.class);
        boolean isStringType = String.class.isAssignableFrom(parameter.getParameterType());
        return hasUserIdAnnotation && isStringType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        return webRequest.getHeader(CLIENT_ID_HEADER);
    }
}