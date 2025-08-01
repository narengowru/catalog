import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.math.BigInteger;

public class LagrangeBigInteger {

    public static void main(String[] args) throws IOException {
        // Process both test cases
        String[] testFiles = { "t1.json", "t2.json" };

        for (int testCase = 0; testCase < testFiles.length; testCase++) {
            System.out.println("\n========== Test Case " + (testCase + 1) + " ==========");
            processTestCase(testFiles[testCase]);
        }
    }

    static void processTestCase(String filename) throws IOException {
        // Read JSON file
        String json = new String(Files.readAllBytes(Paths.get(filename)));

        // Parse JSON manually
        Map<String, Map<String, String>> points = parseJSON(json);

        // Extract n and k from keys
        Map<String, String> keys = points.get("keys");
        int n = Integer.parseInt(keys.get("n"));
        int k = Integer.parseInt(keys.get("k"));

        System.out.println("File: " + filename);
        System.out.println("Number of points (n): " + n);
        System.out.println("Points needed (k): " + k);

        // Collect k points
        List<Point> selectedPoints = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : points.entrySet()) {
            if (entry.getKey().equals("keys"))
                continue;

            String xStr = entry.getKey();
            Map<String, String> pointData = entry.getValue();
            String base = pointData.get("base");
            String value = pointData.get("value");

            // Parse x and y values
            BigInteger x = new BigInteger(xStr);
            BigInteger y = new BigInteger(value, Integer.parseInt(base));

            selectedPoints.add(new Point(x, y));

            System.out.println("Point: x=" + x + ", y=" + y + " (base " + base + ")");

            if (selectedPoints.size() == k)
                break;
        }

        // Calculate f(0) using Lagrange interpolation
        BigInteger secret = lagrangeInterpolation(selectedPoints, BigInteger.ZERO);
        System.out.println("\nThe secret (f(0)) is: " + secret);
        System.out.println("====================================\n");
    }

    static class Point {
        BigInteger x, y;

        Point(BigInteger x, BigInteger y) {
            this.x = x;
            this.y = y;
        }
    }

    static BigInteger lagrangeInterpolation(List<Point> points, BigInteger xValue) {
        BigInteger result = BigInteger.ZERO;
        int n = points.size();

        for (int i = 0; i < n; i++) {
            BigInteger numerator = BigInteger.ONE;
            BigInteger denominator = BigInteger.ONE;

            for (int j = 0; j < n; j++) {
                if (i != j) {
                    // numerator *= (xValue - x[j])
                    numerator = numerator.multiply(xValue.subtract(points.get(j).x));
                    // denominator *= (x[i] - x[j])
                    denominator = denominator.multiply(points.get(i).x.subtract(points.get(j).x));
                }
            }

            // Add y[i] * (numerator / denominator) to result
            // Since we're working with integers, we need to ensure exact division
            BigInteger term = points.get(i).y.multiply(numerator).divide(denominator);
            result = result.add(term);
        }

        return result;
    }

    static Map<String, Map<String, String>> parseJSON(String json) {
        Map<String, Map<String, String>> result = new HashMap<>();

        // Remove whitespace and outer braces
        json = json.replaceAll("\\s+", "");
        json = json.substring(1, json.length() - 1);

        // State machine for parsing
        int i = 0;
        while (i < json.length()) {
            // Find key
            if (json.charAt(i) != '"') {
                i++;
                continue;
            }

            int keyStart = i + 1;
            int keyEnd = json.indexOf('"', keyStart);
            String key = json.substring(keyStart, keyEnd);

            // Skip to value start
            i = keyEnd + 1;
            while (i < json.length() && json.charAt(i) != '{')
                i++;

            if (i >= json.length())
                break;

            // Find matching closing brace
            int braceCount = 1;
            int valueStart = i;
            i++;
            while (i < json.length() && braceCount > 0) {
                if (json.charAt(i) == '{')
                    braceCount++;
                else if (json.charAt(i) == '}')
                    braceCount--;
                i++;
            }

            String valueJson = json.substring(valueStart, i);
            Map<String, String> valueMap = parseObject(valueJson);
            result.put(key, valueMap);

            // Skip comma if present
            if (i < json.length() && json.charAt(i) == ',')
                i++;
        }

        return result;
    }

    static Map<String, String> parseObject(String objJson) {
        Map<String, String> map = new HashMap<>();

        // Remove outer braces
        objJson = objJson.substring(1, objJson.length() - 1);

        // Split by comma (simple split works for non-nested objects)
        String[] pairs = objJson.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].replaceAll("\"", "");
            String value = keyValue[1].replaceAll("\"", "");
            map.put(key, value);
        }

        return map;
    }
}