// See https://github.com/JetBrains/kotlin-examples/blob/master/LICENSE
package com.leonti.receipts

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication(exclude = [SessionAutoConfiguration::class, UserDetailsServiceAutoConfiguration::class])
class Application : SpringBootServletInitializer() {

}

fun main(args: Array<String>) {
	SpringApplication.run(Application::class.java, *args)
}
