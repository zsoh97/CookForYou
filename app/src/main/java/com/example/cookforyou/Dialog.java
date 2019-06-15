package com.example.cookforyou;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Dialog extends AppCompatDialogFragment {

    private EditText addIngredientEditText;
    private AddIngredientDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (AddIngredientDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "must implement AddIngredientDialogListener");
        }
    }

    @Override
    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_dialog, null);

        builder.setView(view).setTitle("Add Ingredient")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ingredient = addIngredientEditText.getText().toString().toLowerCase().trim();
                if(ingredient.isEmpty()){
                    Toast.makeText(getActivity().getApplicationContext(), "Please input a valid ingredient", Toast.LENGTH_SHORT).show();
                } else {
                    listener.applyText(ingredient);
                }
            }
        });

        addIngredientEditText = view.findViewById(R.id.addIngredientEditText);

        return builder.create();
    }

    public interface AddIngredientDialogListener{
        void applyText(String ingredient);
    }
}
