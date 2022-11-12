package org.checksum.aeye;

public class NLPResponse {
    public NLPResponseActionType actionType;
    public String object;

    public NLPResponse(NLPResponseActionType actionType, String object) {
        this.actionType = actionType;
        this.object = object;
    }

    public enum NLPResponseActionType {
        Unknown,
        TurnOn,
        TurnOff,
        Hate,
        Love,
        Order,
        Set,
        Play,
        Destruct,
        Tell,
        Give
    }
}