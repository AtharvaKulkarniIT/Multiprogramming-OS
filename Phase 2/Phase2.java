package org.multiprogramming_os;


import java.io.*;
import java.util.*;

public class Phase2 {
    private char M[][] = new char[300][4];
    private char IR[] = new char[4];
    private int IC;
    private char R[] = new char[4];
    private boolean C;
    private int SI;
    private int PI;
    private int TI;
    private int EM;
    private int PTR;
    private int count;
    private int RA;
    private int TTC;
    private int LLC;
    private int ptrPointer;
    private ArrayList<Integer> AllocateList = new ArrayList<>();


    private String input_File;
    private String output_File;
    private BufferedReader bReader;
    private BufferedWriter bWriter;
    private HashMap<Integer, Integer> VaToRa = new HashMap<>();
    PCB p1 = new PCB(0, 0, 0);


    Phase2(String inputfile, String outputfile) throws Exception {
        this.input_File = inputfile;
        this.output_File = outputfile;
        File fileR = new File(input_File);
        File fileW = new File(output_File);
        bReader = new BufferedReader(new FileReader(fileR));
        bWriter = new BufferedWriter(new FileWriter(fileW));
    }


    private boolean cardReader[] = new boolean[2];
    // 1. LOAD function
    private void LOAD() throws Exception {
        System.out.println("Enter in Load Function");
        String Reader = bReader.readLine();
        System.out.println(Reader);


        while (Reader != null) {
            // Loading Logic
            // Loading Control Card Data
            if (Reader.contains("$AMJ")) {
                // JID,TTL,TLL
                int temp[] = new int[3];
                int j = 0;
                for (int i = 4; i < Reader.length(); i += 4) {
                    temp[j] = Integer.parseInt(Reader.substring(i, i + 4));
                    j++;
                }
                p1 = new PCB(temp[0], temp[1], temp[2]);
                cardReader[0] = true; 
            } else if (Reader.contains("$DTA")) {
                // printPageTable(PTR);
                printPCB(p1);
                STARTEXECUTION();
                cardReader[1] = true;
            } else if (Reader.contains("$END")) {
                printMemory();
                INIT();
            } else if (!Reader.contains("$") && cardReader[0] && !cardReader[1]) {
                int loc = ALLOCATE();
                M[ptrPointer][0] = '1';
                M[ptrPointer][2] = (char) ((loc / 10) + '0');
                M[ptrPointer][3] = (char) ((loc % 10) + '0');
                ptrPointer++;
                int row = loc * 10;
                int col = 0;
                if (Reader.length() > 40) {
                    Reader = Reader.substring(0, 40);
                }
                for (char i : Reader.toCharArray()) {
                    if (row < 300) {
                        M[row][col % 4] = i;
                        col++;
                    } else {
                        System.out.println("Memory Limit Exceeded!!");
                    }
                    if (col % 4 == 0) {
                        row++;
                    }
                }
            }
            Reader = bReader.readLine();
        }
    }


    class PCB {
    int JID;
    int TTL;
    int TLL;


    PCB(int jid, int ttl, int tll) {
        this.JID = jid;
        this.TTL = ttl;
        this.TLL = tll;
    }
}
    // 2. INIT function
    private void INIT() {
        for (char arr[] : this.M) {
            Arrays.fill(arr, ' ');
        }
        this.IC = 0;
        Arrays.fill(this.R, ' ');
        Arrays.fill(this.IR, ' ');
        this.C = false;
        Arrays.fill(this.cardReader, false);
        this.SI = 0;
        this.EM = -1;
        this.PI = 0;
        this.TI = 0;
        this.PTR = 0;
        this.LLC = 0;
        this.TTC = 0;
        this.RA = 0;
        this.AllocateList.clear();
        this.PTR = ALLOCATE() * 10;
        // Initialization of Page Table
        for (int i = PTR; i < PTR + 10; i++) {
            M[i][0] = '0';
            M[i][2] = '*';
            M[i][3] = '*';
        }
        ptrPointer = PTR;
        this.p1 = new PCB(0, 0, 0);
        this.VaToRa.clear();
        count = 0;
    }
    private void printMemory() {
        for (int i = 0; i < M.length; i++) {
            System.out.printf("M[%02d] - ", i);
            for (int j = 0; j < M[i].length; j++) {
                System.out.printf("[ %c ]", M[i][j] == ' ' ? ' ' : M[i][j]);
            }
            System.out.println();
            if ((i + 1) % 10 == 0) {
                System.out.println();
            }
        }
    }
    // 4. STARTEXECUTION program
    private void STARTEXECUTION() throws Exception {
        this.IC = 0;
        EXECUTEUSERPROGRAM();
    }
    // 5. EXECUTEUSERPROGRAM
    private void EXECUTEUSERPROGRAM() throws Exception {
        boolean loop = true;
        while (loop) {
            ADDRESSMAP(IC);
            int j = 0;
            for (char i : M[RA]) {
                IR[j] = i;
                j++;
            }
            IC = IC + 1;
            int operand = (int) (IR[2] - '0') * 10 + (int) (IR[3] - '0');
            MAP(operand);
            operand = RA;
            StringBuilder opcode = new StringBuilder();
            if (IR[0] == 'H') {
                opcode.append(IR[0]);
            } else {
                opcode.append(IR[0]);
                opcode.append(IR[1]);
            }
            switch (opcode.toString()) {
                case "GD":
                    SI = 1;
                    break;
                case "PD":
                    SI = 2;
                    break;
                case "H":
                    SI = 3;
                    loop = false;
                    break;
                case "LR":
                    if (TTC > p1.TTL) {
                        TI = 2;
                        loop = false;
                        return;
                    }
                    j = 0;
                    for (char i : M[operand]) {
                        R[j] = i;
                        j++;
                    }
                    break;
                case "SR":
                    if (TTC > p1.TTL) {
                        TI = 2;
                        loop = false;
                        return;
                    }
                    j = 0;
                    for (char i : R) {
                        M[operand][j] = i;
                        j++;
                    }
                    break;
                case "CR":
                    if (TTC > p1.TTL) {
                        TI = 2;
                        loop = false;
                        return;
                    }
                    int c = 0;
                    j = 0;
                    for (char i : R) {
                        if (M[operand][j] == i) {
                            c++;
                        }
                        j++;
                    }
                    if (c == 4) {
                        this.C = true; 
                    }
                    break;
                case "BT":
                    if (TTC > p1.TTL) {
                        TI = 2;
                        loop = false;
                        return;
                    }
                    if (C) {
                        this.IC = operand;
                    }
                    break;
                default:
                    System.out.println("Invalid Command Or Command Not Found");
                    PI = 1;
                    break;
            }
            SIMULATION();
            if (SI != 0 || PI != 0 || TI != 0) {
                if (MOS(operand) == -1) {
                    loop = false;
                }
                SI = 0;
                PI = 0;
                TI = 0;
            }
        }
    }
    private void SIMULATION() {
        TTC++;
        if (TTC > p1.TTL) {
            TI = 2;
        }
    }
    private int MOS(int operand) throws Exception {
        if (TI == 0) {
            if (PI == 1) {
                TERMINATE(4);
                return -1;
            } else if (PI == 2) {
                TERMINATE(5);
                return -1;
            } else if (PI == 3) {
                TERMINATE(6);
                return -1;
            } else if (SI == 1) {
                return READ(operand);
            } else if (SI == 2) {
                return WRITE(operand);
            } else if (SI == 3) {
                TERMINATE(0);
                return -1;
            }
        } else if (TI == 2) {
            if (PI == 1) {
                TERMINATE(8);
                return -1;
            } else if (PI == 2) {
                TERMINATE(7);
                return -1;
            } else if (PI == 3) {
                TERMINATE(6);
                return -1;
            } else if (SI == 1) {
                TERMINATE(3);
                return -1;
            } else if (SI == 2) {
                WRITE(operand);
                TERMINATE(3);
                return -1;
            } else if (SI == 3) {
                TERMINATE(0);
                return -1;
            } else {
                TERMINATE(3);
                return -1; 
            }
        }
        return 1; 
    }
    private int READ(int location) throws Exception {
        String Data = bReader.readLine();
        if (Data.contains("$END")) {
            TERMINATE(1);
            return -1;
        } else {
            System.out.println(Data);
            int col = 0;
            for (char i : Data.toCharArray()) {
                M[location][col % 4] = i;
                col++;
                if (col % 4 == 0) {
                    location++;
                }
                if (location > 299) {
                    System.out.println("Memory Exceed! " + location);
                    break;
                }
            }
        }
        return 1;
    }
    private int WRITE(int location) throws Exception {
        LLC++;
        if (LLC > p1.TLL) {
            TERMINATE(2);
            return -1;
        } else {
            int col = 0;
            char i = M[location][col];
            StringBuilder Data = new StringBuilder();
            int j = location;
            while (j < location + 10) {
                Data.append(i);
                col++;
                if (col % 4 == 0) {
                    j++;
                }
                if (j > 299) {
                    System.out.println("Memory Exceed! " + j);
                    break;
                }
                i = M[j][col % 4];
            }
            bWriter.write(Data.toString());
            bWriter.newLine();
        }
        return 1;
    }
    private void TERMINATE(int EM) throws Exception {
        String error = "";
        switch (EM) {
            case 0:
                error = "No Error";
                break;
            case 1:
                error = "Out of Data";
                break;
            case 2:
                error = "Line Limit Exceeded";
                break;
            case 3:
                error = "Time Limit Exceeded";
                break;
            case 4:
                error = "Opcode Error";
                break;
            case 5:
                error = "Operand Error";
                break;
            case 6:
                error = "Invalid Page Fault";
                break;
            case 7:
                error = "Time Limit Exceed + Operand Error";
                break;
            case 8:
                error = "Time Limit Exceed + Operation Code Error";
                break;
            default:
                System.out.println("Invalide Error Message");
        }
        bWriter.write("JOB ID \t\t:\t" + p1.JID);
        bWriter.newLine();
        bWriter.write(error);
        bWriter.newLine();
        bWriter.write("IC \t\t\t:\t" + IC);
        bWriter.newLine();
        bWriter.write("IR \t\t\t:\t" + new String(IR));
        bWriter.newLine();
        bWriter.write("TTC \t\t:\t" + TTC);
        bWriter.newLine();
        bWriter.write("LLC \t\t:\t" + LLC);
        bWriter.write("\n");
        bWriter.write("\n");
    }
    private int ALLOCATE() {
        Random rand = new Random();
        int value;
        do {
            value = rand.nextInt(30); 
        } while (AllocateList.contains(value));
        AllocateList.add(value);
        return value;
    }
    private void printPCB(PCB p1) {
        System.out.println("JID : " + p1.JID);
        System.out.println("TTL : " + p1.TTL);
        System.out.println("TLL : " + p1.TLL);
    }
    // Address Map
    private void ADDRESSMAP(int IC) {
        if (IC % 10 == 0 && IC != 0) {
            count++;
        }
        int address = (int) (M[PTR + count][2] - '0') * 10 + (int) (M[PTR + count][3] - '0');
        address = address * 10 + IC % 10;
        RA = address;
    }
    // Add into MAP
    private void MAP(int add) {
        if ((int) (IR[2] - '0') < 0 || (int) (IR[2] - '0') > 9 || (int) (IR[3] - '0') < 0 || (int) (IR[3] - '0') > 9) {
            if (IR[0] != 'H') {
                PI = 2;
                return;
            }
            RA = -1;
            return;
        }
        if (IR[0] == 'B' && IR[1] == 'T') {
            System.out.println("This is BT");
            RA = add;
            return;
        }


        if (VaToRa.containsKey((add / 10) * 10)) {
            RA = VaToRa.get((add / 10) * 10) * 10 + (add % 10);
            return;
        }
        if ((IR[0] == 'G' && IR[1] == 'D') || (IR[0] == 'S' && IR[1] == 'R')) {
            int temp = ALLOCATE();
            VaToRa.put(add, temp);
            M[ptrPointer][0] = '1';
            M[ptrPointer][3] = (char) (temp % 10 + '0');
            M[ptrPointer][2] = (char) (temp / 10 + '0');
            ptrPointer++;
            RA = VaToRa.get(add) * 10;
        } else {
            PI = 3;
            return;
        }
    }
    public static void main(String[] args) throws Exception {
        String InputFile = "C:\\Users\\Hp\\IdeaProjects\\Multiprogramming_OS\\InputForPhase2.txt";
        String OutputFile = "C:\\Users\\Hp\\IdeaProjects\\Multiprogramming_OS\\OutputForPhase2.txt";
        Phase2 p1 = new Phase2(InputFile, OutputFile);
        p1.INIT();
        p1.LOAD();
        p1.bWriter.close();
    }
}


