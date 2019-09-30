import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.PriorityQueue;
import java.util.Random;


public class TestFileGenerator {

	public static void main(String[] args) throws IOException {
		PriorityQueue<Integer> q = new PriorityQueue<Integer>();
		Random r=new Random();
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("NewTestFile_7zeros.txt")));
		PrintWriter sortedOut = new PrintWriter(new BufferedWriter(new FileWriter("sortedout.txt")));
		int count = 10000000;
		out.write(count+" 5mb\n\n");
		for(int i=0;i<count;i++){
			int val = r.nextInt(Integer.MAX_VALUE)+1;
			out.write(String.valueOf(val)+"\n");
			q.add(val);
		}
		for(int i=0;i<count;i++){
			sortedOut.write(String.valueOf(q.poll())+"\n");
		}
		
		out.close();
		sortedOut.close();
		System.out.print("DONE:");
		
	}

}
