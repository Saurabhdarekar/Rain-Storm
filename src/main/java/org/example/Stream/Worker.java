package org.example.Stream;

import org.example.FileSystem.Sender;
import org.example.entities.Member;
import org.example.entities.MembershipList;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

//TODO based on role a Worker thread will be created and a specific function will be called
public class Worker extends Thread {

    List<Member> source;
    List<Member> op1;
    List<Member> op2;
    HashMap<Integer,Member> sources;
    HashMap<Integer,Member> op1s;
    HashMap<Integer,Member> op2s;
    String ranges;
    String type;
    String filename;
    String destFileName;
    String operationName;
    HashMap<Integer, Integer> receiverPorts = new HashMap<>();
    private Sender sender = new Sender();

    int batchId = 0;
    int tupleId = 0;
    public static List<Batch> batchesSent = new CopyOnWriteArrayList<>();
    public static List<Batch> batchesToBeSent= new CopyOnWriteArrayList<>();
    public static List<Batch> batchesReceived = new CopyOnWriteArrayList<>();

    public int receiverPort;

    public Worker(String type, List<Member> source , List<Member> op1, List<Member> op2, String ranges, String filename, String destFileName, String operationName) {
        this.source = source;
        this.op1 = op1;
        this.op2 = op2;
        this.ranges = ranges;
        this.type = type;
        this.filename = filename;
        this.destFileName = destFileName;
        this.operationName = operationName;
    }

    public void setReceiverPorts(ArrayList<String> receiverPorts) {
        for (String port : receiverPorts) {
            String[] s = port.split(",");
            System.out.println("I am saving details for : " + port);
            if(s[0].equals("source")){
                sources.put(Integer.valueOf(s[2]), MembershipList.memberslist.get(Integer.valueOf(s[1])));
            }else if(s[0].equals("op1")){
                op1s.put(Integer.valueOf(s[2]), MembershipList.memberslist.get(Integer.valueOf(s[1])));
            }else{
                op2s.put(Integer.valueOf(s[2]), MembershipList.memberslist.get(Integer.valueOf(s[1])));
            }
            this.receiverPorts.put(Integer.valueOf(s[2]), Integer.valueOf(s[3]));
        }
    }

    //TODO Function : Source
    public void source(){
        //TODO send the data to members present in op1 based on a partition function
        //TODO put the below code in a another jar file
        //TODO Get tuples from the code in object
        //------------------------------
        try {
            String[] range = ranges.split(",");
            int startLine = Integer.parseInt(range[0]);
            int endLine = Integer.parseInt(range[1]);
            Batch batch = new Batch(String.valueOf(batchId), MembershipList.selfId, null);
            try (Scanner scanner = new Scanner(new File(filename))) {
                int currentLine = 0;
                while (scanner.hasNextLine()) {
                    currentLine++;
                    String line = scanner.nextLine();
                    if (currentLine >= startLine && currentLine <= endLine) {
                        //TODO put the line in a form of tuple in the queue
                        batch.getBatchData().add(new Tuple(String.valueOf(tupleId++), currentLine, line));
                        System.out.println(line);
                    }
                    if (currentLine > endLine) {
                        break; // Stop reading once we've passed the desired range
                    }
                    if(batch.getBatchData().size() >= 10){
                        batchesToBeSent.add(batch);
                        batch = new Batch(String.valueOf(batchId), MembershipList.selfId, null);
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //TODO Pass the tuple to the queue which will send it to next nodes.
        //------------------------------
    }

    //TODO Function : Split
    public void op1(){
        //TODO based on num tasks create a connection
    }

    //TODO Function : Count
    public void op2(){

    }

    public void sendBatchData(){

    }

    public void receiveBatchData(){

    }

    public void processop1(List<Tuple>l1){

    }

    public void processAck(String batchId){
        batchesSent.removeIf(batch -> batch.getBatchId().equals(batchId));
    }

    private void processBatches(int OperationStage) throws Exception {
            for(Batch currBatch : batchesReceived){
                //TODO perform some operation
                if(OperationStage==1){
                    //TODO perform some operation & build new tuples data

                    List<Tuple> tuplesForNextStage = new CopyOnWriteArrayList<>();
                    //TODO decide batch id currentl giving random
                    Batch nextStageBatch = new Batch("1", MembershipList.selfId,tuplesForNextStage);
                    batchesToBeSent.add(nextStageBatch);
                    //TODO decide how we are going to send batches , from here or from run of sender

                } else if(OperationStage == 2){
                    //TODO perform operstions and write to Console and HYDFS



                    sender.sendAckToParent(currBatch.getSenderMachineId(),currBatch.getBatchId());
                    //TODO Need to write sendack & receive ack , but where ?
                }

            }
    }


    //TODO Function : run function for the thread
    public void run(){
        switch(type) {
            case "source":
                source();
                break;
            case "op1":
                op1();
                break;
            case "op2":
                op2();
                break;
        }
    }
}
