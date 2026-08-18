package com.sonza.app.core.data.di;

import com.sonza.app.core.data.local.AppDatabase;
import com.sonza.app.core.data.local.dao.DislikedItemDao;
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
public final class DatabaseModule_ProvideDislikedItemDaoFactory implements Factory<DislikedItemDao> {
  private final Provider<AppDatabase> databaseProvider;

  private DatabaseModule_ProvideDislikedItemDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public DislikedItemDao get() {
    return provideDislikedItemDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideDislikedItemDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideDislikedItemDaoFactory(databaseProvider);
  }

  public static DislikedItemDao provideDislikedItemDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDislikedItemDao(database));
  }
}
