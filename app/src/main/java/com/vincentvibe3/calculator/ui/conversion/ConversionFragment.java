package com.vincentvibe3.calculator.ui.conversion;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.vincentvibe3.calculator.R;

public class ConversionFragment extends Fragment {

    private ConversionViewModel ConversionViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ConversionViewModel =
                new ViewModelProvider(this).get(ConversionViewModel.class);
        View root = inflater.inflate(R.layout.fragment_conversion, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        ConversionViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}