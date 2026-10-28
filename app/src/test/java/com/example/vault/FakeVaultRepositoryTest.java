package com.example.vault;

import static org.junit.Assert.assertEquals;

import com.example.vault.fake.FakeVaultRepository;

import org.junit.Test;

public class FakeVaultRepositoryTest {
    @Test
    public void startsWithSampleEntry() {
        FakeVaultRepository repository = new FakeVaultRepository();
        assertEquals(1, repository.observeEntries().getValue().size());
    }
}
