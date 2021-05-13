package dev.romio.cowinvaccinebook.usecase

import kotlinx.coroutines.flow.Flow

abstract class BaseFlowUseCase<Input, Output> {
    abstract fun execute(input: Input): Flow<Output>
}