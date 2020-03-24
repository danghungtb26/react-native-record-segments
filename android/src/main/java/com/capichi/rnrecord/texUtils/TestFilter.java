package com.capichi.rnrecord.texUtils;

public class TestFilter extends TextureRendererDrawOrigin {
    private static final String fshEmboss = "" +
            "precision mediump float;" +
            " varying vec2 vTextureCoord;\n" +
            " \n" +
            " uniform lowp sampler2D sTexture;\n" +
            " uniform highp float exposure;\n" +
            " \n" +
            " void main()\n" +
            " {\n" +
            "     highp vec4 textureColor = texture2D(sTexture, vTextureCoord);\n" +
            "     \n" +
            "     gl_FragColor = vec4(textureColor.rgb * pow(2.0, exposure), textureColor.w);\n" +
            " } ";

    protected static final String SAMPLER_STEPS = "exposure";
    private float exposure = 1f;

    public static TextureRendererEmboss create(boolean isExternalOES) {
        TextureRendererEmboss renderer = new TextureRendererEmboss();
        if(!renderer.init(isExternalOES)) {
            renderer.release();
            return null;
        }
        return renderer;
    }

    @Override
    public boolean init(boolean isExternalOES) {
        if(setProgramDefualt(getVertexShaderString(), getFragmentShaderString(), isExternalOES)) {
            mProgram.bind();
            mProgram.sendUniformf(SAMPLER_STEPS, exposure);
            return true;
        }
        return false;
    }

    @Override
    public void setTextureSize(int w, int h) {
        super.setTextureSize(w, h);
        mProgram.bind();
        mProgram.sendUniformf(SAMPLER_STEPS, exposure);
    }


    @Override
    public String getFragmentShaderString() {
        return fshEmboss;
    }
}
