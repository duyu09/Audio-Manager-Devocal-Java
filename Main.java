//Copyright (c) 2022 Qilu University of Technology, School of Computer Science and Technology, Software Engineering(Development) 21-1, Duyu.No.202103180009.
//All rights reserved.
//Version 1.0.0
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

public class Main
{
    public static String INPUT_FILENAME="C:\\Users\\35834\\Desktop\\CORE.wav";
    public static String OUTPUT_FILENAME="C:\\Users\\35834\\Desktop\\123.wav";
    public static int SampleRate;
    public static int BitDepth;
    public static int ChannelNumber;
    public static int DataBegin;
    public static void main(String[] args)
    {
        File inputFile=new File(INPUT_FILENAME);
        File outputFile=new File(OUTPUT_FILENAME);
        FileInputStream fileInputStream01 = null;
        FileOutputStream fileOutputStream01 = null;
        try
        {
            outputFile.createNewFile();
            fileInputStream01=new FileInputStream(inputFile);
            fileOutputStream01=new FileOutputStream(outputFile);
        }
        catch (Exception e01)
        {
            System.out.println(e01.getMessage());
        }

        byte[] data=new byte[(int) inputFile.length()];
        try
        {
            fileInputStream01.read(data);
        }
        catch (Exception e02)
        {
            System.out.println(e02.getMessage());
        }
        String[] temp = ReadPcmData(data,0,null);
        System.out.println("采样率："+temp[0]+" Hz");
        System.out.println("位深度："+temp[1]+" bits");
        System.out.println("DATA区块位置："+temp[2]);
        System.out.println("通道数："+temp[3]+" Channels");

        ProcessSubMixer(data,DataBegin,SampleRate,BitDepth,ChannelNumber);
        try
        {
            fileOutputStream01.write(data);
        }
        catch (Exception e05)
        {
            System.out.println(e05.getMessage());
        }
    }
    static int bytesToInt(byte[] arr)
    {
        int result=0;int a,b,c,d;
        if(arr.length == 4)
        {
            a = (arr[3] & 0xff) << 24;
            b = (arr[2] & 0xff) << 16;
            c = (arr[1] & 0xff) << 8;
            d = (arr[0] & 0xff);
            result = a | b | c | d;
        }
        else if (arr.length == 3)
        {
            b = (arr[2] & 0xff) << 16;
            c = (arr[1] & 0xff) << 8;
            d = (arr[0] & 0xff);
            result = b | c | d;
        }
        else if (arr.length == 2)
        {
            c = (arr[1] & 0xff) << 8;
            d = (arr[0] & 0xff);
            result = c | d;
        }
        else if (arr.length == 1)
        {
            d = (arr[0] & 0xff);
            result = d;
        }
        return result;
    }
    static byte[] intToBytes(int number,int bitDepth)
    {
        byte[] temp=null;
        if(bitDepth==8)
        {
            temp = new byte[]{(byte)number};
        }
        else if(bitDepth==16)
        {
            temp=new byte[2];
            temp[1] = (byte)(number >>> 8);
            temp[0] = (byte)(number >>> 0);
        }
        else if(bitDepth==24)
        {
            temp=new byte[3];
            temp[2] = (byte)(number >>> 16);
            temp[1] = (byte)(number >>> 8);
            temp[0] = (byte)(number >>> 0);
        }
        else if(bitDepth==32)
        {
            temp=new byte[4];
            temp[3] = (byte)(number >>> 24);
            temp[2] = (byte)(number >>> 16);
            temp[1] = (byte)(number >>> 8);
            temp[0] = (byte)(number >>> 0);
        }
        return temp;
    }
    static String[] ReadPcmData(byte[] data, int length_of_head, Object reserved)
    {
        //返回数组：0:采样率(HZ)，1:位深度(bytes/sample)，2:DATA区块起始位置，3:声道数
        byte[] byteArr_SampleRate=new byte[]{data[24],data[25],data[26],data[27]};
        byte[] byteArr_ChannelNumber=new byte[]{data[22],data[23]};
        byte[] byteArr_BitDepth=new byte[]{data[32],data[33]};
        SampleRate=bytesToInt(byteArr_SampleRate);
        ChannelNumber=bytesToInt(byteArr_ChannelNumber);
        BitDepth=bytesToInt(byteArr_BitDepth)*4;
        int count01=1;StringBuffer stringBuffer01=new StringBuffer();
        while(true)
        {
            try
            {
                stringBuffer01.append((char)data[count01]);
                stringBuffer01.append((char)data[count01+1]);
                stringBuffer01.append((char)data[count01+2]);
                stringBuffer01.append((char)data[count01+3]);
            }
            catch (Exception e03){}
            if(stringBuffer01.toString().equals("data"))
            {
                break;
            }
            stringBuffer01=new StringBuffer();
            ++count01;
        }
        DataBegin=count01+4;
        String[] returnStringArr=new String[4];
        returnStringArr[0]=String.valueOf(SampleRate);
        returnStringArr[1]=String.valueOf(BitDepth);
        returnStringArr[2]=String.valueOf(DataBegin);
        returnStringArr[3]=String.valueOf(ChannelNumber);
        return returnStringArr;
    }
    static void ProcessSubMixer(byte[]data, int in_DataBegin, int in_SampleRate,int in_BitDepth,int ChannelNumber)
    {
        if(ChannelNumber!=2)
        {
            System.err.println("错误，该PCM音频的通道数不为2，不支持重混缩");
        }
        int sampleByte=in_BitDepth/8;
        byte[] subDataArrLEFT;byte[] subDataArrRIGHT;
        int subDataLEFT;int subDataRIGHT;
        for(int a=in_DataBegin;a<=data.length-100;a+=sampleByte*2)
        {
            subDataArrLEFT=Arrays.copyOfRange(data,a,a+sampleByte);
            subDataArrRIGHT=Arrays.copyOfRange(data,a+sampleByte,a+2*sampleByte);
            subDataLEFT=bytesToInt(subDataArrLEFT);
            subDataRIGHT=bytesToInt(subDataArrRIGHT);
            subDataArrLEFT=intToBytes(subDataLEFT-subDataRIGHT,in_BitDepth);
            //System.out.println(subDataLEFT+subDataRIGHT);
            subDataArrRIGHT=subDataArrLEFT;
            System.arraycopy(subDataArrLEFT,0,data,a,sampleByte);
            System.arraycopy(subDataArrRIGHT,0,data,a+sampleByte,sampleByte);
        }
    }
}
