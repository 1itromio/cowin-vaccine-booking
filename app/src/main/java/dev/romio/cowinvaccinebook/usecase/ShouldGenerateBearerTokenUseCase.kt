package dev.romio.cowinvaccinebook.usecase

import dev.romio.cowinvaccinebook.repository.CowinAppRepository
import javax.inject.Inject

class ShouldGenerateBearerTokenUseCase @Inject constructor(
    private val cowinAppRepository: CowinAppRepository
): BaseUseCase<Unit, Boolean>(){
    override suspend fun execute(input: Unit): Boolean {
        val tokenExpTime = cowinAppRepository.getTokenExpiryTime()
        val currTime = System.currentTimeMillis() / 1000
        return tokenExpTime == 0L || currTime > (tokenExpTime-5L)
    }
}