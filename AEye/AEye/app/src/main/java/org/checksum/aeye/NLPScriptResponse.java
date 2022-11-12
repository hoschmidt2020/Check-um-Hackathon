package org.checksum.aeye;

import java.util.ArrayList;

import edu.stanford.nlp.pipeline.CoreDocument;

public class NLPScriptResponse {
    public ArrayList<NLPResponse> responses;
    public CoreDocument document;

    public NLPScriptResponse(ArrayList<NLPResponse> responses, CoreDocument document) {
        this.responses = responses;
        this.document = document;
    }
}
