package com.sonza.app.core.data.di;

import com.sonza.app.core.data.local.AppDatabase;
import com.sonza.app.core.data.local.dao.ListeningHistoryDao;
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
public final class DatabaseModule_ProvideListeningHistoryDaoFactory implements Factory<ListeningHistoryDao> {
  private final Provider<AppDatabase> databaseProvider;

  private DatabaseModule_ProvideListeningHistoryDaoFactory(Provider<AppDatabase> databaseProvider) {
    this.databaseProvider = databaseProvider;
  }

  @Override
  public ListeningHistoryDao get() {
    return provideListeningHistoryDao(databaseProvider.get());
  }

  public static DatabaseModule_ProvideListeningHistoryDaoFactory create(
      Provider<AppDatabase> databaseProvider) {
    return new DatabaseModule_ProvideListeningHistoryDaoFactory(databaseProvider);
  }

  public static ListeningHistoryDao provideListeningHistoryDao(AppDatabase database) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideListeningHistoryDao(database));
  }
}
