package com.munchies.user

import com.munchies.user.api.AccountApi
import com.munchies.user.model.AuthResponse
import com.munchies.user.model.MePutRequest
import com.munchies.user.model.RegisterPostRequest
import com.munchies.user.model.User
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.runtime.Micronaut
import reactor.core.publisher.Mono

@Controller
class AccountApiImpl : AccountApi {
    override fun confirmEmailGet(token: String): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun meGet(): Mono<User> {
        println("meGet")
        val user = User()
        return Mono.just(user)
    }

    override fun mePut(mePutRequest: MePutRequest): Mono<Void> {
        TODO("Not yet implemented")
    }

    override fun registerPost(registerPostRequest: RegisterPostRequest): Mono<HttpResponse<AuthResponse>> {
        TODO("Not yet implemented")
    }

}

fun main(args: Array<String>){
    Micronaut.run(*args)
}