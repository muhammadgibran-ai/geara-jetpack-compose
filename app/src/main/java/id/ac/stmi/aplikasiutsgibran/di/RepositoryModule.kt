package id.ac.stmi.aplikasiutsgibran.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import id.ac.stmi.aplikasiutsgibran.data.GearaRepository
import id.ac.stmi.aplikasiutsgibran.data.GearaRepositoryImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGearaRepository(
        gearaRepositoryImpl: GearaRepositoryImpl
    ): GearaRepository
}
