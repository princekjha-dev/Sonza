package com.sonza.app.core.data.di;

import com.sonza.app.core.data.local.AppDatabase;
import com.sonza.app.core.data.local.dao.SongGenreDao;
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
public final class DatabaseModule_ProvideSongGenreDaoFactory implements Factory<SongGenreDao> {
  private final Provider<AppDatabase> databaseProvider;

  private DatabaseModule_ProvideSongGenreDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public SongGenreDao get() {
    return provideSongGenreDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideSongGenreDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideSongGenreDaoFactory(databaseProvider);
  }

  public static SongGenreDao provideSongGenreDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSongGenreDao(database));
  }
}
