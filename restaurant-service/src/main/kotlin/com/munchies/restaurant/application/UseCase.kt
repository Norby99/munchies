package com.munchies.restaurant.application

interface UseCase<in C, out R> {
  suspend operator fun invoke(command: C): R
}
