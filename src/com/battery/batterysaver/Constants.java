package com.battery.batterysaver;

public class Constants {
	
	public static final String APPLICATION_FOLDER = "/sdcard/heuristicdownload/";
	public static final String DEFAULT_DOWNLOAD_CONFIG = "/sdcard/heuristicdownload/download.txt";
	public static final String DEFAULT_STORE_DIRECTORY = "/sdcard/heuristicdownloa/downloads/";
	
	public static int LOGGING = 100;
	
	public static int AUTO_MODE = 0;

	public static int DECLINE_STEP = 10000;
	
	public static double RECOVERY_RATE = 0.8;
	
	public static int PAR_FOR_BETA = 10;
	
	public static int STARTING_POINT = 500;
	
	public static int SCREEN_ACTIVE = 0;
	
	public static int TESTING_SECOND = 3600;
	
	public static int TESTING_FILE_SIZE = 1 << 30; // 100 * (1 << 20);
	
	public static double REST_TO_DOWNLOAD = 0.8;
	
	public static double DOWNLOAD_TO_REST = 1 - REST_TO_DOWNLOAD;
	
	public static final int FIRST_DOWNLOAD_TIME = 10; //10s. When downloading the file with auto mode, first download for 10s

	public static final int SAMPLE_NUM = 5;
}
