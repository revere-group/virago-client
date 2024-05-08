/*originals https://www.shadertoy.com/view/XcySDz*/
uniform float time;
uniform vec2 mouse;
uniform vec2 resolution;
uniform int iFrame;
#extension GL_EXT_gpu_shader4 : enable
#extension GL_ARB_gpu_shader5 : enable
#extension GL_ARB_shader_bit_encoding : enable
vec3 hash33(vec3 ip) {
    uvec3 p = floatBitsToUint(ip);
    uvec3 k = ~p;
    p ^= p << 17U; p ^= p >> 13U;
    p ^= p << 5U;
    p += ((k * 512U) >> 7U) * 34125U;
    p.y += p.x ^= k.z ^= p.z;
    p.z += p.y ^= k.x ^= p.x;
    p.x += p.z ^= k.y ^= p.y;
    p *= 501304U;
    return vec3(p) / float(0xFFFFFFFFU);
}

vec3 noise33(vec3 p) {
    vec3 id = floor(p);
    vec3 lv = fract(p);
    lv = lv*lv*(3.0-2.0*lv);
    return mix(mix(
                   mix(hash33(id + vec3(0, 0, 0)), hash33(id + vec3(1, 0, 0)), lv.x),
                   mix(hash33(id + vec3(0, 1, 0)), hash33(id + vec3(1, 1, 0)), lv.x),
                   lv.y
               ), mix(
                   mix(hash33(id + vec3(0, 0, 1)), hash33(id + vec3(1, 0, 1)), lv.x),
                   mix(hash33(id + vec3(0, 1, 1)), hash33(id + vec3(1, 1, 1)), lv.x),
                   lv.y
               ), lv.z);
}

vec3 noise33(vec3 p, float freq, int oct) {
    vec3 n = vec3(0.0);
    float amp = 1.0;
    float div = 0.0;

    for (int i = min(iFrame, 0); i < oct; i++) {
        n += amp * noise33(p * freq);
        div += amp;
        amp *= 0.5;
        freq *= 2.0;
    }

    return n / div;
}
float sigmoid(float x) {
    return 1.0f / (1.0 + exp(-x));
}
vec3 sigmoid(vec3 v) {
    return vec3(sigmoid(v.x), sigmoid(v.y), sigmoid(v.z));
}
mat2 rot(float a) { float c = cos(a); float s = sin(a); return mat2(c, s, -s, c); }

vec3 wave(in vec3 p, float freq, int oct, in float eps, vec4 m){
    vec3 a = noise33(p, freq, oct);

    float di = 0.0;

    if (m.z > 0.001) {
        vec3 n = normalize(a*2.0-1.0);
        vec2 uv = mix(mix(p.xy, p.yz, abs(dot(n, vec3(1, 0, 0)))), p.xz, abs(dot(n, vec3(0, 1, 0))));
        di = dot(uv, m.xy*freq);
    }

    vec2 e = vec2(eps + (di*eps*2.0), 0.0);

    return abs(a.x - vec3(
        noise33(p - e.xyy, freq, oct).x,
        noise33(p - e.yxy, freq, oct).x,
        noise33(p - e.yyx, freq, oct).x
    ));

}



void mainImage(out vec4 o, in vec2 fc)
{
    vec3 col = vec3(0.0);
    vec2 uv = (fc.xy - 0.5 * resolution.xy) / resolution.y;
    float t2 = time * .1 + ((.25 + .05 * sin(time * .1))/(length(uv.xy) + .07)) * 3.2;
    float si = sin(t2);
    float co = cos(t2);
    mat2 ma = mat2(co, si, -si, co);

    uv*=ma;
    uv.x+=cos(time);
    uv.y+=sin(time);
    vec4 m = vec4((mouse.xy - 0.5 * resolution.xy) / resolution.y, mouse.yx);

    vec3 rd = normalize(vec3(uv.xy, 1.0));
    float eps = 0.06;
    float freq = 1.6;
    int oct = 6;
    float t = time*0.1;
    vec3 sh = vec3(sin(t), cos(t), 1.0);

    vec3 w =
    wave(rd + sh.xyz + wave(rd + sh.yzx + wave(rd + sh.zxy,
                                               freq, oct, eps, m),
                            freq, oct, eps, m),
         freq, oct, eps, m);

    vec3 N = normalize(w);
    vec3 L = normalize(vec3(1, 2, -1));
    vec3 ref = reflect(N, L);
    float VdotR = max(0.0, dot(rd, ref));
    float spec = pow(VdotR, 64.0)*2.;
    float NdotL = max(0.01, dot(N, L));
    vec3 dif = vec3(0.005, 0.39, 0.76);
    col += NdotL * (spec + dif);
    col = col*col;
    col += col*col;
    col += 0.25*sigmoid(20.0*col-3.);
    col = max(vec3(0.0), col);
    col = pow(col, vec3(1.0 / 2.2));

    o = vec4(col, 1.0);
}

void main(void)
{
    mainImage(gl_FragColor, gl_FragCoord.xy);
}