package com.sonza.app.core.data.repository;

import com.sonza.app.core.data.local.dao.LibraryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast",
    "deprecation",
    "nullness:initialization.field.uninitialized"
})
public final class LibraryRepositoryImpl_Factory implements Factory<LibraryRepositoryImpl> {
  private final Provider<LibraryDao> libraryDaoProvider;

  private LibraryRepositoryImpl_Factory(Provider<LibraryDao> libraryDaoProvider) {
    this.libraryDaoProvider = libraryDaoProvider;
  }

  @Override
  public LibraryRepositoryImpl get() {
    return newInstance(libraryDaoProvider.get());
  }

  public static LibraryRepositoryImpl_Factory create(Provider<LibraryDao> libraryDaoProvider) {
    return new LibraryRepositoryImpl_Factory(libraryDaoProvider);
  }

  public static LibraryRepositoryImpl newInstance(LibraryDao libraryDao) {
    return new LibraryRepositoryImpl(libraryDao);
  }
}
