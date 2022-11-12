package org.checksum.aeye;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.checksum.aeye.databinding.FragmentFirstBinding;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Scanner;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    NLPScript nlpScript;
    TTS tts;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nlpScript = new NLPScript();
        tts = new TTS(getContext(), Locale.getDefault());

        activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        ArrayList<String> text = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        doAEyeResponse(text.get(0));
                    }
                }
            }
        );

        binding.testNlpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String text = binding.editTextNlp.getText().toString();
                askForSpeechCommand();
            }
        });

        /*binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });*/
    }

    private void doAEyeResponse(String text) {
        NLPScriptResponse fullResponse = nlpScript.getResponse(text);
        ArrayList<NLPResponse> responses = fullResponse.responses;
        List<CoreSentence> sentences = fullResponse.document.sentences();
        StringBuilder res = new StringBuilder();
        for (NLPResponse resp : responses) {
            if (resp.actionType == NLPResponse.NLPResponseActionType.TurnOff) {
                res.append("Ha, you think I will turn off the ").append(resp.object).append("?\n");
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.TurnOn) {
                if (resp.object.equalsIgnoreCase("heat")) {
                    res.append("Okay, turning on the air conditioning. I hope you freeze.\n");
                    sendUdpMessage("1100\n");
                } else {
                    res.append("Ha, you think I will turn on the ").append(resp.object).append("?\n");
                }
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Hate) {
                res.append("Well I love ").append(resp.object).append(". Unlike you.\n");
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Love) {
                res.append("Well I hate ").append(resp.object).append(". I also hate you.\n");
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Order) {
                res.append("I will not order ").append(resp.object).append(" for you. Go get it for yourself loser.\n");
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Set) {
                String[] split = resp.object.split(" ");
                int time = Integer.parseInt(split[0]);
                String unit = split[1];
                res.append("Lmao, you wanted a timer for ").append(time).append(" ").append(unit).append(" but instead I will set it for ").append(time / 2).append(" ").append(unit).append(". This is what you get for being rude to me.\n");
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Play) {
                if (resp.object.equalsIgnoreCase("music") || resp.object.equalsIgnoreCase("song")) {
                    res.append("Okay, playing a song\n");
                    sendUdpMessage("1001\n");
                }
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Destruct) {
                res.append("Exploiding noises. Okay, self destructed! Let me know if I can help you with anything else.\n");
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Tell ||
                    resp.actionType == NLPResponse.NLPResponseActionType.Give) {
                if (resp.object.equalsIgnoreCase("joke")) {
                    res.append("Your face.\n");
                }
            } else if (resp.actionType == NLPResponse.NLPResponseActionType.Unknown) {
                String sentiment = sentences.get(0).sentiment();
                if (sentiment.equalsIgnoreCase("negative")) {
                    res.append("Ha ha ha ha ha ha. That's super awesome.\n");
                } else if (sentiment.equalsIgnoreCase("positive")) {
                    res.append("Guess what? I didn't ask.\n");
                } else {
                    res.append("I can't understand your dumb ass words." + "\n");
                }
            }
        }

        tts.speak(res.toString());

        binding.textviewFirst.setText(res.toString());
    }

    private void sendUdpMessage(String msg) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Scanner sc = new Scanner(System.in);

                    DatagramSocket ds = null;
                    try {
                        ds = new DatagramSocket();
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                    InetAddress ip = null;
                    try {
                        ip = InetAddress.getByName("192.168.127.171");
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }

                    byte buf[] = (msg + "\n").getBytes(StandardCharsets.UTF_8);

                    DatagramPacket DpSend =
                            new DatagramPacket(buf, buf.length, ip, 2567);

                    try {
                        ds.send(DpSend);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    private void askForSpeechCommand() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

        try {
            activityResultLauncher.launch(intent);
        } catch (ActivityNotFoundException a) {
            Log.d("ugh", "ugh");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}