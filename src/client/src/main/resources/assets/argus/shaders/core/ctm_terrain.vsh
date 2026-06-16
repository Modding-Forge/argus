#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:globals.glsl>
#moj_import <minecraft:chunksection.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;
uniform usamplerBuffer ArgusCtmPayload;
uniform samplerBuffer ArgusCtmSourceUv;
uniform usamplerBuffer ArgusCtmOverlayMaterials;
uniform samplerBuffer ArgusCtmMaterials;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec4 shadeColor;
out vec4 lightColor;
out vec2 texCoord0;
flat out uint argusCtmPayload;
flat out vec4 argusCtmSourceUv;
flat out uvec4 argusCtmOverlayMaterials;
flat out vec4 argusCtmMaterialUv;
flat out int argusCtmVertexId;

void main() {
    vec3 pos = Position + (ChunkPosition - CameraBlockPos) + CameraOffset;
    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    lightColor = sample_lightmap(Sampler2, UV2);
    shadeColor = Color;
    vertexColor = Color * lightColor;
    texCoord0 = UV0;
    argusCtmVertexId = gl_VertexID;
    argusCtmPayload = texelFetch(ArgusCtmPayload, gl_VertexID).r;
    argusCtmSourceUv = texelFetch(ArgusCtmSourceUv, gl_VertexID);
    argusCtmOverlayMaterials = texelFetch(
            ArgusCtmOverlayMaterials, gl_VertexID);
    argusCtmMaterialUv = texelFetch(ArgusCtmMaterials,
            int(argusCtmPayload & 0xFFFFu));
}
