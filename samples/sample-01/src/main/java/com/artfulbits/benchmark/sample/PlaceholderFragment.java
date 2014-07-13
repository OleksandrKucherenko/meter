package com.artfulbits.benchmark.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/** Login fragment with simplest UI. */
public class PlaceholderFragment extends Fragment {

  private Button btnProceed;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = inflater.inflate(R.layout.fragment_main, container, false);

    btnProceed = (Button) view.findViewById(R.id.bt_proceed);

    return view;
  }
}
