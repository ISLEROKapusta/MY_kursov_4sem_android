package com.example.myapplicationkurs;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.eazegraph.lib.charts.PieChart;
import org.eazegraph.lib.models.PieModel;

public class GraphFragment extends Fragment {

    // Ключи для передачи аргументов
    private static final String ARG_VALUE_R = "value_r";
    private static final String ARG_VALUE_P = "value_p";

    // Объявляем объекты TextView и PieChart
    TextView tvR, tvP;
    PieChart pieChart;

    // Переменные для хранения переданных значений
    private int valueR;
    private int valueP;

    public static GraphFragment newInstance(int valueR, int valueP) {
        GraphFragment fragment = new GraphFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_VALUE_R, valueR);
        args.putInt(ARG_VALUE_P, valueP);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            valueR = getArguments().getInt(ARG_VALUE_R);
            valueP = getArguments().getInt(ARG_VALUE_P);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // "Надуваем" макет для этого фрагмента
        View view = inflater.inflate(R.layout.fragment_graph, container, false);

        // Связываем представления с их соответствующими ID
        tvR = view.findViewById(R.id.tvR);
        tvP = view.findViewById(R.id.tvP);
        pieChart = view.findViewById(R.id.piechart);

        // Устанавливаем данные для текстовых представлений и круговой диаграммы
        setData();

        return view;
    }

    private void setData() {
        // Устанавливаем процентное соотношение использования языков
        tvR.setText(Integer.toString(valueR));
        tvP.setText(Integer.toString(valueP));

        // Устанавливаем данные и цвет для круговой диаграммы
        pieChart.addPieSlice(
                new PieModel(
                        "Затраты",
                        Integer.parseInt(tvR.getText().toString()),
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "Зачисления",
                        Integer.parseInt(tvP.getText().toString()),
                        Color.parseColor("#66BB6A")));

        // Анимируем круговую диаграмму
        pieChart.startAnimation();
    }
}