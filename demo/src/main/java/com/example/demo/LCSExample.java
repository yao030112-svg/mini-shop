package com.example.demo;

public class LCSExample {
    public static void main(String[] args) {
        String s1 = "ABCBDAB";
        String s2 = "BDCABA";
        System.out.println(lcsLength(s1, s2));
    }

    public static int lcsLength(String s1, String s2) {
        int m = s1.length();
        int n = s2.length();
        int [] d= new int[n+1];
        for(int i=1;i<m;i++){
            int prev=0;
            for(int j=1;j<=n;j++){
                int temp=d[j];
                if(s1.charAt(i-1)==s2.charAt(j-1)){
                    d[j]=prev+1;
                }else {
                    d[j]=Math.max(d[j],d[j-1]);
                }
                prev=temp;
            }
        }
        return d[n];
    }
}
