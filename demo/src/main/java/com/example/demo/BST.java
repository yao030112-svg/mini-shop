package com.example.demo;

public class BST{
    class Node{
        int key;
        String val;
        Node left,right;
        Node(int k,String v){
            key=k;
            val=v;
        }
    }
    private Node root;
    //插入
    public void add(int k,String val){
        root=add(root,k,val);
    }
    private Node add(Node node,int key,String val){
        //空结点
        if(node==null){
            return new Node(key,val);
        }
        if(key> node.key){
            node.right=(node.right, key,val);
        }
        else if(key< node.key){
            node.left=(node.left,key,val);
        }else
        {
            node.val=val;
        }
        return node;
    }


}
