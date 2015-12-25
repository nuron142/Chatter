package com.nuron.chatter;

import com.cloudinary.Cloudinary;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by nuron on 25/12/15.
 */
public class Utilities {

    public static final int IMAGE_QUALITY_50 = 50;
    public static final int IMAGE_QUALITY_30 = 30;


    public static Cloudinary getCloudinaryInstance() {
        Map config = new HashMap();
        config.put("cloud_name", "n07t21i7");
        config.put("api_key", "123456789012345");
        config.put("api_secret", "abcdeghijklmnopqrstuvwxyz12");
        return new Cloudinary(config);
    }

    public static String[] getHalfAndFullResolutionUrl(int imageHeightHalf, int imageWidthHalf,
                                                       int imageHeightFull, int imageWidthFull,
                                                       String imagePath) {

        // Image Url : https://res.cloudinary.com/nuron/image/upload/
        //                                 c_fill,h_150,w_200/oyhgaekvoojfibb70ixz.webp

        String baseUrl = "https://res.cloudinary.com/nuron/image/upload/";
        String fillParam = "c_fill";
        String imageExtension = ".webp";

        String imageHeightHalfParam = "h_" + imageHeightHalf;
        String imageWidthHalfParam = "w_" + imageWidthHalf;

        String imageHeightFullParam = "h_" + imageHeightFull;
        String imageWidthFullParam = "w_" + imageWidthFull;

        String qualityHalfParam, qualityFullParam;

        qualityHalfParam = ",q_" + Utilities.IMAGE_QUALITY_30;
        qualityFullParam = ",q_" + Utilities.IMAGE_QUALITY_50;


        String imageUrlResolutionHalf = baseUrl + fillParam + "," +
                imageHeightHalfParam + "," + imageWidthHalfParam +
                qualityHalfParam + "/" + imagePath + imageExtension;

        String imageUrlResolutionFull = baseUrl + fillParam + "," +
                imageHeightFullParam + "," + imageWidthFullParam +
                qualityFullParam + "/" + imagePath + imageExtension;

        return new String[]{imageUrlResolutionHalf, imageUrlResolutionFull};
    }

}
