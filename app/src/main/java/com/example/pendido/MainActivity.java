package com.example.pendido;
/**
 * @author Thomas DA SILVA PENAS, Amina ABADI et Sokhna LO
 * @version 1.0
 */

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView tvListeLettres;                      //Lettres en haut à gauche
    private ImageView ivPendu;                            //Image du panda
    private LinearLayout wordContainer;                   //Lettres du mot
    private EditText etLetter;                            //Lettre à taper
    private Button btnSend;                               //Bouton d'envoi
    private TextView tvEnigme;                            //Texte d'enigme
    private String word = new String();                   //Le mot à trouver
    private String enonce = new String();                 //L'enigme
    private String enigme = new String();                 //L'enoncé de l'énigme
    private int found;                                    //Nombre de lettres trouvées
    private int error;                                    //Nombre d'erreurs
    private List<Character> list = new ArrayList<>();     //Liste des lettres tapées
    private boolean win;                                  //Booléen de victoire (true = trouvé, false = perdu)
    private final int nbEnigmes = 14;                     //Nombre d'enigmes (Firestore compliqué)
    private static final String TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wordContainer = findViewById(R.id.wordContainer);
        btnSend = findViewById(R.id.btnSend);
        etLetter = findViewById(R.id.etLetter);
        ivPendu = findViewById(R.id.ivPendu);
        tvListeLettres = findViewById(R.id.tvListeLettres);
        tvEnigme = findViewById(R.id.tvEnigme);

        init();

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = etLetter.getText().toString().toUpperCase();
                etLetter.setText("");

                if (input.length() > 0) {
                    if (!letterUsed(input.charAt(0), list)) {
                        list.add(input.charAt(0));
                        checkLetterIsInWord(input, word);
                    }

                    if (found == word.length()) {
                        win = true;
                        createDialog(win);
                    }

                    if (!word.contains(input)) {
                        error++;
                    }

                    setImage(error);

                    if (error == 6) {
                        createDialog(win);
                    }

                    //Affiche les lettres utilisés
                    showUsedLetters(list);
                }
            }
        });
    }

    /**
     * Initialise le jeu et la connexion au Cloud Firestore
     */
    public void init() {
        int random = (int) (Math.random() * nbEnigmes) + 1;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("enigmes")
                .document("" + random)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        setWord(documentSnapshot.getString("reponse"));
                        //System.out.println("DEBUG1//Le mot à trouver : " + word);
                        win = false;
                        found = 0;
                        error = 0;
                        tvListeLettres.setText("");
                        ivPendu.setBackgroundResource(R.drawable.pendu1);
                        wordContainer.removeAllViews();
                        list = new ArrayList<>();
                        enonce = documentSnapshot.getString("enonce");

                        for (int i = 0; i < word.length(); i++) {
                            TextView oneLetter = (TextView) getLayoutInflater().inflate(R.layout.textview, null);
                            wordContainer.addView(oneLetter);
                        }

                        tvEnigme.setText(enonce);
                        //System.out.println("DEBUG2//Le mot à trouver : " + word);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                    }
                });

    }

    /**
     * Setter qui initialise le mot à trouver
     * @param w : le mot à trouver
     */
    public void setWord(String w) {
        this.word = w.toUpperCase();
    }

    /**
     *  Vérifie si la lettre donnée en paramètre est dans la liste des lettres tapées
     * @param c     la lettre donnée à tester
     * @param list  la liste des lettre tapées
     * @return true si la lettre est dans la liste, false sinon
     */
    public boolean letterUsed(char c, List<Character> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == c) {
                Toast.makeText(getApplicationContext(), "Vous avez déjà tapé cette lettre", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie que la lettre donnée est contenue dans le mot
     * @param letter    la lettre donnée à tester
     * @param word      le mot
     */
    public void checkLetterIsInWord(String letter, String word) {
        for (int i = 0; i < word.length(); i++) {
            if (letter.equals(String.valueOf(word.charAt(i)))) {
                TextView tv = (TextView) wordContainer.getChildAt(i);
                tv.setText(String.valueOf(word.charAt(i)));
                found++;
            }
        }
    }

    /**
     * Affiche les lettres utilisée qui sont contenues dans la liste list
     * @param list  la liste des lettres utilisées
     */
    public void showUsedLetters(List<Character> list) {
        String string = "";
        for (int i = 0; i < list.size(); i++) {
            string += list.get(i) + "\n";
        }

        if (!string.equals("")) {
            tvListeLettres.setText(string);
        }
    }

    /**
     * Affiche l'image du panda correspondant au nombre d'erreur commises
     * @param error  le nombre d'erreur commises
     */
    public void setImage(int error) {
        switch (error) {
            case 1:
                ivPendu.setBackgroundResource(R.drawable.pendu2);
                break;
            case 2:
                ivPendu.setBackgroundResource(R.drawable.pendu3);
                break;
            case 3:
                ivPendu.setBackgroundResource(R.drawable.pendu4);
                break;
            case 4:
                ivPendu.setBackgroundResource(R.drawable.pendu5);
                break;
            case 5:
                ivPendu.setBackgroundResource(R.drawable.pendu6);
                break;
            case 6:
                ivPendu.setBackgroundResource(R.drawable.pendu7);
                break;
        }
    }

    /**
     * Affiche l'alertDialog en fonction du booléen win, selon si on a trouvé le mot ou pas
     * @param win   le booléan qui nous indique si on a trouvé le mot ou pas
     */
    public void createDialog(boolean win) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Vous avez gagné !");

        if (!win) {
            builder.setTitle("Vous avez perdu !");
            builder.setMessage("Le mot à trouvé était : " + word);
        }
        builder.setPositiveButton(getResources().getString(R.string.rejouer), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                init();
            }
        });
        builder.create().show();
    }

}