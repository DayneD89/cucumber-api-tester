package uk.ddou.cucumber;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
import cucumber.runtime.Runtime;


public class Main {

    private static String featurePath = System.getProperty("user.dir")+File.separator+"features";
    private static String outputPath = System.getProperty("user.dir")+File.separator+"output";

    public static void main(String args[]) {
        disableWarning();

        if(args.length==0)
            while(true) {
                System.out.println("Tests Found: "+getFoundTests());
                String testTag=getInput("What tag should I test next?","^[a-z,A-Z,0-9,-,_]*$");
                if(testTag.toLowerCase().equals("exit") || testTag.toLowerCase().equals("quite"))
                    break;
                runTest(testTag);
        } else {
            for(int i=0;i<args.length;i++) {
               System.out.println("Running @"+args[0]+" tests...");
               runTest(args[i]);
            }
        }
    }

    public static String getFoundTests() {
        ArrayList<String> tagsFound = new ArrayList<>();
        String output="";
        File[] files = new File(featurePath).listFiles();
        for (File file : files) {
            try {
                if (!(file.isDirectory()) && file.getName().toLowerCase().contains(".feature")) {
                    BufferedReader brTest = new BufferedReader(new FileReader(file));
                    String text = brTest.readLine().trim();
                    String[] tags=text.split(" ");
                    for(int i=0;i<tags.length;i++) {
                        if(tags[i].contains("@") && !tags[i].equals("@ignore")) {
                            tags[i] = tags[i].replace("@","");
                            tagsFound.add(tags[i]);
                        }
                    }
                }
            } catch(Exception e) {}
        }
        tagsFound = removeDuplicates(tagsFound);
        for(int i=0;i<tagsFound.size();i++) {
            output+=tagsFound.get(i)+", ";
        }
        if(output.endsWith(", ")) {output=output.substring(0,output.length()-2);}
        return output;
    }

    private static ArrayList<String> removeDuplicates(ArrayList<String> input) {
        ArrayList<String> output = new ArrayList<>();
        for(int i=0;i<input.size();i++) {
            if(!(output.contains(input.get(i)))) {
                output.add(input.get(i));
            }
        }
        return output;
    }

    private static String getInput(String question, String regex) {
        Scanner reader = new Scanner(System.in);
        String response="";
        while(response.equals("")) {
            System.out.println(question);
            String input = reader.nextLine().trim();
            if(input.matches(regex)) {response=input;}
        }
        return response;
    }
    public static void disableWarning() {
        System.err.close();
        System.setErr(System.out);
    }

    public static byte runTest(String testTag) {
        byte b = 0;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String[] args = new String[] {
                    "--tags",
                    "@"+testTag,
                    "--glue",
                    "uk.ddou.cucumber",
                    "--plugin",
                    "null_summary",
                    "--plugin",
                    "html:"+outputPath,
                    featurePath
            };

            final Runtime runtime = Runtime.builder()
                    .withArgs(args)
                    .withClassLoader(classLoader)
                    .build();

            runtime.run();
            b=runtime.exitStatus();
            System.out.println("TestResults saved at "+System.getProperty("user.dir")+File.separator+"output"+File.separator+"index.html\n");
        }
        catch (Exception e) {
            System.out.println("EXCEPTION: "+e.getMessage()+"\n");
            e.printStackTrace();
        }
        return b;
    }
}
