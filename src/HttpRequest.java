import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;



public class HttpRequest {
	protected static String requestTo(HttpURLConnection conn) throws IOException {
		StringBuffer res = new StringBuffer();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			String buf = "";
			while ((buf = in.readLine()) != null) {
				res.append(buf);
			}
		}
		
		return res.toString();
	}
	
	public static String get(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		conn.setRequestProperty("Content-Type","application/json"); 
		
		return HttpRequest.requestTo(conn);
	}
	
	public static String get(String u) throws IOException {
		URL url = new URL(u);
		
		return HttpRequest.get(url);
	}
	
	public static String get(URL url, Map<String, String> param) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("GET");
		
		StringBuffer urlParam = new StringBuffer();
		urlParam.append("?");
		for (String key : param.keySet()) {
			String val = param.get(key);
			urlParam.append(key).append("=").append(val).append("&");
		}
		
		String urlWithParam = urlParam.substring(0, urlParam.length()-1);
		return HttpRequest.get(new URL(url.toString() + urlWithParam));
	}
	
	public static String get(String u, Map<String, String> param) throws IOException {
		URL url = new URL(u);
		return HttpRequest.get(url, param);
	}
	
	public static String post(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		return HttpRequest.requestTo(conn);
	}
	
	public static String post(String u) throws IOException {
		return HttpRequest.post(new URL(u));
	}
	
	public static String post(URL url, Map<String, String> param) throws IOException {
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		
		ObjectMapper mapper = new ObjectMapper();
		String jsonParam = mapper.writeValueAsString(param);
		
		conn.setDoOutput(true);
		try (DataOutputStream writer = new DataOutputStream(conn.getOutputStream())) {
			writer.writeBytes(jsonParam);
		}
		
		return HttpRequest.requestTo(conn);
	}
	
	public static String post(String u, Map<String, String> param) throws IOException {
		return HttpRequest.post(new URL(u), param);
	}
	
	/**
	 * 여러개의 파일을 한 번에 업로드
	 * @param url: 업로드할 URL
	 * @param param: 같이 업로드 할  Form Field
	 * @param fileParams: 같이 업로드 할 File Field (name, filePath를 동시에 지정해 주어야 한다)
	 * @return Json: 응답 메시지
	 * @throws IOException
	 */
	public static String postMultipart(String url, Map<String, String> param, List<Map<String, String>> fileParams)
			throws IOException {
		
		MultipartUtility multipart = new MultipartUtility(url, "UTF-8");
		for (String key : param.keySet()) {
			multipart.addFormField(key, param.get(key));
		}
		
		for (Map<String, String> fileParam : fileParams) {
			String filePath = fileParam.get("filePath");
			String name = fileParam.get("name");
			
			File file = new File(filePath);
			multipart.addFilePart(name, file);
		}
		
		List<String> listRes = multipart.finish();
		StringBuffer buffer = new StringBuffer();
		for (String res : listRes) {
			buffer.append(res);
		}
		
		return buffer.toString();
	}
	
	/**
	 * 하나의 파일을 한 번에 업로드
	 * @param url: 업로드할 URL
	 * @param param: 같이 업로드 할  Form Field
	 * @param fileParams: 같이 업로드 할 File Field (name, filePath를 동시에 지정해 주어야 한다)
	 * @return Json: 응답 메시지
	 * @throws IOException
	 */
	public static String postMultipart(String url, Map<String, String> param, Map<String, String> fileParam)
			throws IOException {
		
		MultipartUtility multipart = new MultipartUtility(url, "UTF-8");
		for (String key : param.keySet()) {
			multipart.addFormField(key, param.get(key));
		}
		
		String filePath = fileParam.get("filePath");
		String name = fileParam.get("name");
		
		File file = new File(filePath);
		multipart.addFilePart(name, file);
		
		List<String> listRes = multipart.finish();
		StringBuffer buffer = new StringBuffer();
		for (String res : listRes) {
			buffer.append(res);
		}
		
		return buffer.toString();
	}
	
	public static void main(String[] args) throws IOException {
		Map<String, String> param = new HashMap<>();
		param.put("userId", "sdzaq@naver.com");
		
		Map<String, String> fileParam = new HashMap<>();
		fileParam.put("name", "photo");
		fileParam.put("filePath", "C:/Users/Ahn/Pictures/nyang.jpg");
		
		String res = HttpRequest.postMultipart("	/photos/users/", param, fileParam);
		System.out.println(res);
	}
}
