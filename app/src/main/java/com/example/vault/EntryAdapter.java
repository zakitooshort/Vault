package com.example.vault;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.vault.data.VaultEntry;
import com.example.vault.databinding.ItemEntryBinding;

import java.util.ArrayList;
import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.EntryViewHolder> {
    public interface OnEntryClickListener {
        void onEntryClick(VaultEntry entry);
    }

    private final OnEntryClickListener listener;
    private final List<VaultEntry> allEntries = new ArrayList<>();
    private final List<VaultEntry> visibleEntries = new ArrayList<>();

    public EntryAdapter(OnEntryClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VaultEntry> newEntries) {
        allEntries.clear();
        if (newEntries != null) {
            allEntries.addAll(newEntries);
        }
        filter("");
    }

    public void filter(String query) {
        String normalizedQuery = query == null ? "" : query.trim().toLowerCase();
        visibleEntries.clear();
        for (VaultEntry entry : allEntries) {
            String title = entry.getTitle() == null ? "" : entry.getTitle().toLowerCase();
            String username = entry.getUsername() == null
                    ? "" : entry.getUsername().toLowerCase();
            if (normalizedQuery.isEmpty()
                    || title.contains(normalizedQuery)
                    || username.contains(normalizedQuery)) {
                visibleEntries.add(entry);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEntryBinding binding = ItemEntryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new EntryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        holder.bind(visibleEntries.get(position));
    }

    @Override
    public int getItemCount() {
        return visibleEntries.size();
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {
        private final ItemEntryBinding binding;

        EntryViewHolder(ItemEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(VaultEntry entry) {
            binding.titleText.setText(entry.getTitle());
            binding.usernameText.setText(entry.getUsername());
            binding.getRoot().setOnClickListener(v -> listener.onEntryClick(entry));
        }
    }
}
