package bgu.spl.net.impl.stomp;

import java.util.HashMap;
import java.util.Map;

public class Frame {
    //fields:
    private String command;
    private Map<String,String> headers;
    private String body;

    // constructors:
    Frame(){
        command = new String();
        headers = new HashMap<String,String>();
        body = new String();
    }

    Frame(String _command , HashMap<String,String> _headers , String _body){
        command = _command;
        headers = _headers;
        body = _body;
    }

    Frame(String msg){
        int pointer = 0;
        //command:
        String c = new String();
        while(msg.charAt(pointer)!='\n'){
            c += msg.charAt(pointer);
            pointer++;
        } 
        pointer++;
        //headers:
        HashMap<String,String> h = new HashMap<>();
        String currKey= new String();
        String currVal= new String();
        while(msg.charAt(pointer) !='\n'){
            while(msg.charAt(pointer)!=':'){
                currKey += msg.charAt(pointer);
                pointer++;
            }
            pointer++;
            while(msg.charAt(pointer)!='\n'){
                currVal += msg.charAt(pointer);
                pointer++;
            }
            pointer++;
            h.put(currKey, currVal);
            currKey = "";
            currVal = "";
        }
        pointer++;
        // body:
        String b = new String();
        b = msg.substring(pointer);
        // Update relevant fields:
        command = c;
        headers = h;
        body = b;
    }

    /**
     * uses ONLY for errorFrame
     * @param commandCausedError
     * @param receipt
     * @param mistakeMsg
     */
    Frame(String commandCausedError, String receipt, String mistakeMsg){
        command = "ERROR";
        headers = new HashMap<>();
        if (receipt != null){
            headers.put("receipt-id", receipt);
        }
        headers.put("error caused by command", commandCausedError);
        headers.put("message", mistakeMsg);
        body=new String();
    }

    public String getCommand() {
        return command;
    }

    public Map<String,String> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    /**
     * returns a string representing the frame
     * @return returns a string representing the frame
    */
    @Override
    public String toString() {
        String output = new String(); 
        output = output + command + '\n';
        // for each header:value pair in the headers:
        for (HashMap.Entry<String, String> pair : headers.entrySet()){
            output += pair.getKey();
            output += ":";
            output += pair.getValue();
            output += '\n';
        }
        output += '\n'; // end of headers

        if (body != null) { 
            output += body;
            // output += '\u0000';// null char is already inserted in other location
        }
        return output;
    }

}
