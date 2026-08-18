package com.sonza.app.core.data.di;

import com.sonza.app.core.data.local.AppDatabase;
import com.sonza.app.core.data.local.dao.LyricsDao;
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
public final class DatabaseModule_ProvideLyricsDaoFactory implements Factory<LyricsDao> {
  private final Provider<AppDatabase> databaseProvider;

  private DatabaseModule_ProvideLyricsDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public LyricsDao get() {
    return provideLyricsDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideLyricsDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideLyricsDaoFactory(databaseProvider);
  }

  public static LyricsDao provideLyricsDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideLyricsDao(database));
  }
}
