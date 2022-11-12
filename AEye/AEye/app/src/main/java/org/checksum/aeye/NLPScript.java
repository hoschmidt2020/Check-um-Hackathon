package org.checksum.aeye;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreSentence;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;

public class NLPScript {
    StanfordCoreNLP nlp;

    public NLPScript() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,pos,parse,sentiment");
        props.setProperty("coref.algorithm", "neural");
        nlp = new StanfordCoreNLP(props);
    }

    public NLPScriptResponse getResponse(String text) {
        text = text.trim();
        // make sentence end with period if it doesn't
        if (!text.endsWith(".")) {
            text += ".";
        }

        ArrayList<NLPResponse> responses = new ArrayList<>();

        CoreDocument document = new CoreDocument(text);
        nlp.annotate(document);
        CoreSentence sentence = document.sentences().get(0);
        List<String> verbPhrases = sentence.verbPhrases();
        for (int verbPhraseIdx = 0; verbPhraseIdx < verbPhrases.size(); verbPhraseIdx++) {
            String verbPhrase = verbPhrases.get(verbPhraseIdx);

            NLPResponse.NLPResponseActionType actionType = NLPResponse.NLPResponseActionType.Unknown;
            String object = "unknown";

            String verbPhraseLower = verbPhrase.toLowerCase();
            String[] words = verbPhraseLower.split(" ");
            for (int i = 0; i < words.length - 1; i++) {
                if (words[i].equals("turn")) {
                    if (words[i+1].equals("on")) {
                        actionType = NLPResponse.NLPResponseActionType.TurnOn;
                    } else if (words[i+1].equals("off")) {
                        actionType = NLPResponse.NLPResponseActionType.TurnOff;
                    }
                } else if (words[i].equals("hate")) {
                    actionType = NLPResponse.NLPResponseActionType.Hate;
                } else if (words[i].equals("love")) {
                    actionType = NLPResponse.NLPResponseActionType.Love;
                } else if (words[i].equals("order")) {
                    actionType = NLPResponse.NLPResponseActionType.Order;
                } else if (words[i].equals("set")) {
                    actionType = NLPResponse.NLPResponseActionType.Set;
                } else if (words[i].equals("play")) {
                    actionType = NLPResponse.NLPResponseActionType.Play;
                } else if (words[i].equals("destruct")) {
                    actionType = NLPResponse.NLPResponseActionType.Destruct;
                } else if (words[i].equals("tell")) {
                    actionType = NLPResponse.NLPResponseActionType.Tell;
                } else if (words[i].equals("give")) {
                    actionType = NLPResponse.NLPResponseActionType.Give;
                }
            }

            if (actionType != NLPResponse.NLPResponseActionType.Unknown) {
                object = getTreeItem(sentence.verbPhraseTrees().get(verbPhraseIdx), new String[] {"(NNS ", "(NN ", "(NNP"});
            }

            if (actionType == NLPResponse.NLPResponseActionType.Set && object.equals("timer")) {
                String num = getTreeItem(sentence.verbPhraseTrees().get(verbPhraseIdx).getChild(2), new String[] {"(CD "});
                String unit = getTreeItem(sentence.verbPhraseTrees().get(verbPhraseIdx).getChild(2), new String[] {"(NNS ", "(NN ", "(NNP"});
                object = num + " " + unit;
            }

            responses.add(new NLPResponse(actionType, object));
        }

        return new NLPScriptResponse(responses, document);
        //String sentiment = sentence.sentiment();
    }

    private String getTreeItem(Tree tree, String[] checks) {
        String pennString = tree.pennString();
        for (String check : checks) {
            if (pennString.startsWith(check)) {
                return pennString.substring(check.length(), pennString.length() - 2);
            }
        }

        for (Tree child : tree.children()) {
            String possibleNoun = getTreeItem(child, checks);
            if (!possibleNoun.equals("<null>")) {
                return possibleNoun;
            }
        }

        return "<null>";
    }
}
