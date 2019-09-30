import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Yash Sheth,Samip Thakkar
 *
 */
public class TPMWMS {
	static float number_of_tuples = 0;
	static float main_memory_size = 0;
	public static void main(String[] args) {
		long start_time = System.currentTimeMillis();

		boolean info_flag = false;
		String file_path = "NewTestFile_5zeros.txt";

		try {
			File file = new File(file_path);
			String validationMessage = validateFile(file);
			if (!validationMessage.equalsIgnoreCase("Valid File")) {
				System.out.println("Invalid File. Please choose correct file.");
			}

			BufferedReader reader = new BufferedReader(new FileReader(file_path));
			String line = reader.readLine();

			if (line != null && !line.isEmpty() && !line.equals("")
					&& !info_flag) {
				String file_info[] = line.split(" ");
				number_of_tuples = Integer.valueOf(file_info[0]);
				main_memory_size = Integer.valueOf(file_info[1].substring(0,
						file_info[1].length() - 2));
				info_flag = true;
			}

			System.out.println("Number of tuples are: " + number_of_tuples);
			System.out.println("Main memory size is: " + main_memory_size);
			System.out.println(reader.readLine());
			reader.close();
			if (info_flag) {
				List<File> l = sortTempFiles(file);
				Comparator<Integer> comparator = new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						if (o1 > o2)
							return -1;
						else if (o2 > o1)
							return 1;
						else
							return 0;
					}
				};

				
				int tuples_in_output=mergeSortedTempFiles(l, new File("OutputFile.txt"), comparator);

				System.out.println("\nOUTPUT FILE HAS "+tuples_in_output+" TUPLES.");
				
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		long end_time   = System.currentTimeMillis();
		long total_time = end_time - start_time;
		System.out.println("TOTAL TIME TAKEN TO SORT THE FILE IS "+total_time+"ms.");
	}

	/**
	 * This method checks that file is not empty and is in proper format
	 * @param file
	 * @return String
	 */
	public static String validateFile(File file) {
		if (!file.exists()) {
			return "File does not exist.";
		}
		String name = file.getName();
		String extension = name.substring(name.lastIndexOf(".") + 1);
		if (!extension.equalsIgnoreCase("txt")) {
			return "Invalid extension of File. Please provide the correct file.";
		}
		if (file.length() == 0) {
			return "File is empty please select correct file.";
		}
		return "Valid File";
	}

	/** This method divides main file into number of temporary files for sorting
	 * @param file
	 * @return List of File
	 * @throws IOException
	 */
	public static List<File> sortTempFiles(File file) throws IOException {
		List<File> files = new ArrayList<File>();
		long blocksize = getBlockSize(file);
		int current_block_size=0;
		
		BufferedReader reader = new BufferedReader(new FileReader(file));
		try {
			reader.readLine();
			reader.readLine();
			List<Integer> temp_list = new ArrayList<Integer>();
			String line = "";
			
			try {
				while (line != null) {
					current_block_size = 0;
					while ((current_block_size < blocksize) && ((line = reader.readLine()) != null)) {
						temp_list.add(Integer.valueOf(line));
						current_block_size += 40;
					}
					if(line!=null)
						files.add(sortAndSave(temp_list));
					temp_list.clear();
				}
			} catch (EOFException oef) {
				if (temp_list.size() > 0) {
					files.add(sortAndSave(temp_list));
					temp_list.clear();
				}
			}
		} finally {
			System.out.println("TEMPORARY FILES : " + files.size());
			reader.close();
		}
		return files;
	}

	/** This method will sort and save the temporary files for merging
	 * @param tmplist
	 * @return file
	 * @throws IOException
	 */
	public static File sortAndSave(List<Integer> temp_list) throws IOException {
		Collections.sort(temp_list);
		File new_temp_file = File.createTempFile("sortInBatch", "flatfile");
		new_temp_file.deleteOnExit();

		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(new_temp_file)));

		try {
			for (int r : temp_list) {
				writer.write(String.valueOf(r)+"\n");
			}
		} finally {

	            writer.close();
		}
		return new_temp_file;
	}

	/** This method will calculate best block size for sorting
	 * @param filetobesorted
	 * @return long
	 */
	public static long getBlockSize(File filetobesorted) {
		int size_of_file = (int) (number_of_tuples * 4);
		final Float MAXTEMPFILES = (float) Math.ceil(((number_of_tuples * 4) / (main_memory_size * 1000000)));
		long blocksize = (long) (size_of_file / MAXTEMPFILES);
		System.out.println("BLOCK SIZE : " + blocksize+"bytes");
		return blocksize;
	}
	
	/** This method merges the sorted temporary files into single output file
	 * @param files
	 * @param outputfile
	 * @param cmp
	 * @return
	 * @throws IOException
	 */
	public static int mergeSortedTempFiles(List<File> files, File output_file,final Comparator<Integer> cmp) throws IOException {
		PriorityQueue<BinaryFileBuffer> pq = new PriorityQueue<BinaryFileBuffer>(11, new Comparator<BinaryFileBuffer>() {
					public int compare(BinaryFileBuffer i, BinaryFileBuffer j) {						
						return i.peek() - j.peek();
					}
				});
		for (File f : files) {
			
			BinaryFileBuffer bfb = new BinaryFileBuffer(f);
			pq.add(bfb);
		}
		PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(output_file)));
		int row_counter = 0;
		try {
			while (pq.size() > 0) {
				
				BinaryFileBuffer bfb = pq.poll();
				int r = bfb.pop();
				writer.write(String.valueOf(r)+"\n");
				++row_counter;
				
				if (bfb.empty()) {
					bfb.reader.close();
					bfb.originalfile.delete();
				} else {
					pq.add(bfb);
				}
			}
		} finally {
			writer.close();
			for (BinaryFileBuffer bfb : pq)
				bfb.close();
		}
		return row_counter;
	}
}


class BinaryFileBuffer {
	public static int BUFFERSIZE = (int) (1024*8);
	public BufferedReader reader;
	public File originalfile;
	private String cache;
	private boolean empty;

	public BinaryFileBuffer(File f) throws IOException {
		originalfile = f;
		reader = new BufferedReader(new FileReader(f),BUFFERSIZE);
		reload();
	}

	public boolean empty() {
		return empty;
	}

	private void reload() throws IOException {
		try {
			if ((this.cache = reader.readLine()) == null) {
				empty = true;
				cache = null;
			} else {
				empty = false;
			}
		} catch (EOFException oef) {
			empty = true;
			cache = null;
		}
	}

	public void close() throws IOException {
		reader.close();
	}

	public int peek() {
		return Integer.valueOf(cache);
	}

	public int pop() throws IOException {
		if(empty()) return (Integer) null;
		int answer = peek();
		reload();
		return answer;
	}

}
