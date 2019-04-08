#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv/cv.hpp>

extern "C"
{
JNIEXPORT jstring JNICALL
Java_com_example_opencvsample_MainActivity_version(
        JNIEnv *env,
        jobject) {
    std::string version = cv::getVersionString();
    return env->NewStringUTF(version.c_str());
}

JNIEXPORT jbyteArray
JNICALL Java_com_example_opencvsample_MainActivity_rgba2bgra
        (
                JNIEnv *env,
                jobject obj,
                jint w,
                jint h,
                jbyteArray src
        ) {
        // Obtaining element row
        // Need to release at the end
        jbyte *p_src = env->GetByteArrayElements(src, NULL);
        if (p_src == NULL) {
            return NULL;
        }

        // Convert arrangement to cv::Mat
        cv::Mat m_src(h, w, CV_8UC4, (u_char *) p_src);
        cv::Mat m_dst(h, w, CV_8UC4);

        // Jarain78
        // Resize the image to 75 %
        int img_width = m_src.size().width;
        int img_height = m_src.size().height;

        // OpenCV process
        cv::cvtColor(m_src, m_dst, CV_RGBA2BGRA);

        // Add Circle to the image
        cv::circle(m_dst, cv::Point(50, 50), 50, cv::Scalar(0, 0, 255), CV_FILLED, 8, 0);

        cv::resize(m_src, m_dst, cv::Size(), 0.75, 0.75);

        // Pick out arrangement from cv::Mat
        u_char *p_dst = m_dst.data;

        // Assign element for return value use
        jbyteArray dst = env->NewByteArray(w * h * 4);
        if (dst == NULL) {
            env->ReleaseByteArrayElements(src, p_src, 0);
            return NULL;
        }
        env->SetByteArrayRegion(dst, 0, w * h * 4, (jbyte *) p_dst);

        // release
        env->ReleaseByteArrayElements(src, p_src, 0);

        return dst;

    }

    JNIEXPORT jbyteArray
    JNICALL Java_com_example_opencvsample_MainActivity_rgba2gray
    (
            JNIEnv *env,
    jobject obj,
            jint w,
    jint h,
            jbyteArray src
    ) {
    // Obtaining element row
    // Need to release at the end
    jbyte *p_src = env->GetByteArrayElements(src, NULL);
    if (p_src == NULL) {
    return NULL;
    }

    // Convert arrangement to cv::Mat
    cv::Mat m_src(h, w, CV_8UC4, (u_char *) p_src);
    cv::Mat m_dst(h, w, CV_8UC4);

    // Jarain78
    // Resize the image to 75 %
    int img_width = m_src.size().width;
    int img_height = m_src.size().height;

    // OpenCV process
    cv::cvtColor(m_src, m_dst, CV_RGBA2GRAY);

    // Pick out arrangement from cv::Mat
    u_char *p_dst = m_dst.data;

    // Assign element for return value use
    jbyteArray dst = env->NewByteArray(w * h * 1);

    if (dst == NULL) {
        env->ReleaseByteArrayElements(src, p_src, 0);
        return NULL;
    }
    env->SetByteArrayRegion(dst, 0, w * h * 1, (jbyte *) p_dst);

    // release
    env->ReleaseByteArrayElements(src, p_src, 0);

    return dst;

    }

    JNIEXPORT jbyteArray
    JNICALL Java_com_example_opencvsample_MainActivity_flipimage
        (
                JNIEnv *env,
                jobject obj,
                jint w,
                jint h,
                jbyteArray src
        ) {
    // Obtaining element row
    // Need to release at the end
    jbyte *p_src = env->GetByteArrayElements(src, NULL);
    if (p_src == NULL) {
        return NULL;
    }

    // Convert arrangement to cv::Mat
    cv::Mat m_src(h, w, CV_8UC4, (u_char *) p_src);
    cv::Mat m_dst(h, w, CV_8UC4);

    cv::Mat flip_dst = cv::Mat(m_src.rows, m_src.cols, CV_8UC3);
    cv::flip(m_src, flip_dst, 0);

    // Pick out arrangement from cv::Mat
    u_char *p_dst = flip_dst.data;

    // Assign element for return value use
    jbyteArray dst = env->NewByteArray(w * h * 4);

    if (dst == NULL) {
        env->ReleaseByteArrayElements(src, p_src, 0);
        return NULL;
    }
    env->SetByteArrayRegion(dst, 0, w * h * 4, (jbyte *) p_dst);

    // release
    env->ReleaseByteArrayElements(src, p_src, 0);

    return dst;

}
}