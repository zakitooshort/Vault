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
    private final List<VaultEntry> entries = new ArrayList<>();

    public EntryAdapter(OnEntryClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<VaultEntry> newEntries) {
        entries.clear();
        if (newEntries != null) {
            entries.addAll(newEntries);
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
        holder.bind(entries.get(position));
    }

    @Override
    public int getItemCount() {
        return entries.size();
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
