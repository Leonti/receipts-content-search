package com.leonti.receipts.auth

import com.google.appengine.api.appidentity.AppIdentityServiceFactory
import com.google.appengine.api.utils.SystemProperty
import com.google.appengine.tools.cloudstorage.*
import com.google.common.io.CharStreams
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.channels.Channels
import java.util.logging.Logger

@Configuration
@EnableWebSecurity
class ApiSecurityConfig : WebSecurityConfigurerAdapter() {
    private val log = Logger.getLogger(ApiSecurityConfig::class.java.name)

    private fun gcsService(): GcsService {
        return GcsServiceFactory.createGcsService(RetryParams.Builder()
                .initialRetryDelayMillis(10)
                .retryMaxAttempts(10)
                .totalRetryPeriodMillis(15000)
                .build())
    }

    private fun getStoredApiKey():String {
        val defaultBucket = AppIdentityServiceFactory.getAppIdentityService().defaultGcsBucketName
        log.info("Deafault bucket name $defaultBucket")
        val readChannel = gcsService().openReadChannel(GcsFilename(defaultBucket, "api-key"), 0)
        return CharStreams.toString(InputStreamReader(Channels.newInputStream(readChannel), Charsets.UTF_8)).trim()
    }

    private fun createLocalToken() {
        val defaultBucket = AppIdentityServiceFactory.getAppIdentityService().defaultGcsBucketName
        val outputChannel = gcsService().createOrReplace(GcsFilename(defaultBucket, "api-key"), GcsFileOptions.getDefaultInstance());
        val outputStream = Channels.newOutputStream(outputChannel)
        OutputStreamWriter(outputStream).use { outputStreamWriter ->
            outputStreamWriter.write("LOCAL_API_KEY")
        }
    }

    private fun getApiKey(): String {
        if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
            createLocalToken()
        }
        return getStoredApiKey()
    }

    override fun configure(httpSecurity: HttpSecurity) {
        val apiKey = getApiKey()

        val filter = ApiAuthFilter()
        filter.setAuthenticationManager(AuthenticationManager { authentication ->
            val principal = authentication.principal as String
            if ("ApiKey $apiKey" != principal) {
                throw BadCredentialsException("The API key was not found or not the expected value.")
            }
            authentication.isAuthenticated = true
            authentication
        })

        httpSecurity.antMatcher("/api/**")
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilter(filter).authorizeRequests().anyRequest().authenticated()
    }
}
