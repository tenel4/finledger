package com.finledger.ledger_service.adapter.outbound.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProcessedMessageRepositoryAdapterTest {

  private ProcessedMessageJpaRepository repo;
  private ProcessedMessageRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    repo = mock(ProcessedMessageJpaRepository.class);
    adapter = new ProcessedMessageRepositoryAdapter(repo);
  }

  @Test
  void markProcessedIfNew_whenAlreadyExists_shouldReturnFalseAndNotSave() {
    UUID messageId = UUID.randomUUID();
    when(repo.existsById(messageId)).thenReturn(true);

    boolean result = adapter.markProcessedIfNew(messageId);

    assertThat(result).isFalse();
    verify(repo, times(1)).existsById(messageId);
    verifyNoMoreInteractions(repo);
  }

  @Test
  void markProcessedIfNew_whenNotExists_shouldSaveAndReturnTrue() {
    UUID messageId = UUID.randomUUID();
    when(repo.existsById(messageId)).thenReturn(false);

    boolean result = adapter.markProcessedIfNew(messageId);

    assertThat(result).isTrue();
    verify(repo, times(1)).existsById(messageId);
    verify(repo, times(1)).save(argThat(entity -> entity.getMessageId().equals(messageId)));
    verifyNoMoreInteractions(repo);
  }
}
