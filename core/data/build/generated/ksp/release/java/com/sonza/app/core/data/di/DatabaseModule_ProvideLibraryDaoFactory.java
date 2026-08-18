package com.sonza.app.core.data.di;

import com.sonza.app.core.data.local.AppDatabase;
import com.sonza.app.core.data.local.dao.LibraryDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;

@ScopeMetadata
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
public final class DatabaseModule_ProvideLibraryDaoFactory implements Factory<LibraryDao> {
  private final Provider<AppDatabase> databaseProvider;

  private DatabaseModule_ProvideLibraryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LibraryDao get() {
    return provideLibraryDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideLibraryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideLibraryDaoFactory(databaseProvider);
  }

  public static LibraryDao provideLibraryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLibraryDao(database));
  }
}
