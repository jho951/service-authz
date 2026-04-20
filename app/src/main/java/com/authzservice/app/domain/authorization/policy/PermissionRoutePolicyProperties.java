package com.authzservice.app.domain.authorization.policy;

import com.authzservice.app.domain.authorization.model.PermissionCode;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "permission.route-policy")
public class PermissionRoutePolicyProperties {

    private List<Route> routes = defaultRoutes();

    public List<Route> getRoutes() {
        return routes;
    }

    public void setRoutes(List<Route> routes) {
        this.routes = routes == null || routes.isEmpty() ? defaultRoutes() : routes;
    }

    private static List<Route> defaultRoutes() {
        List<Route> routes = new ArrayList<>();
        routes.add(new Route(List.of("GET", "HEAD", "OPTIONS", "POST", "PUT", "PATCH", "DELETE"), List.of("/admin/manage", "/admin/manage/**", "/v1/admin/manage", "/v1/admin/manage/**"), PermissionCode.ADMIN_MANAGE));
        routes.add(new Route(List.of("GET", "HEAD", "OPTIONS"), List.of("/admin", "/admin/**", "/v1/admin", "/v1/admin/**"), PermissionCode.ADMIN_READ));
        routes.add(new Route(List.of("POST", "PUT", "PATCH"), List.of("/admin", "/admin/**", "/v1/admin", "/v1/admin/**"), PermissionCode.ADMIN_WRITE));
        routes.add(new Route(List.of("DELETE"), List.of("/admin", "/admin/**", "/v1/admin", "/v1/admin/**"), PermissionCode.ADMIN_DELETE));
        return routes;
    }

    public record Route(List<String> methods, List<String> patterns, PermissionCode permission) {
    }
}
