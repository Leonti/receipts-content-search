package com.leonti.receipts.auth

import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter
import javax.servlet.http.HttpServletRequest

class ApiAuthFilter : AbstractPreAuthenticatedProcessingFilter() {
    override fun getPreAuthenticatedCredentials(request: HttpServletRequest?): Any = "N/A"

    override fun getPreAuthenticatedPrincipal(request: HttpServletRequest?): Any =
            request?.getHeader("Authorization").orEmpty()
}