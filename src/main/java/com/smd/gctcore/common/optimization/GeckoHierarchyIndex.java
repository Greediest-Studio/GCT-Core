package com.smd.gctcore.common.optimization;

import software.bernie.geckolib3.geo.raw.pojo.Bone;
import software.bernie.geckolib3.geo.raw.pojo.MinecraftGeometry;
import software.bernie.geckolib3.geo.raw.pojo.RawGeoModel;
import software.bernie.geckolib3.geo.raw.tree.RawBoneGroup;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GeckoHierarchyIndex {
    private GeckoHierarchyIndex() {}

    /** 返回 null 时由 GeckoLib 原方法处理，保留异常模型的诊断行为。 */
    public static RawGeometryTree tryBuild(RawGeoModel model) {
        MinecraftGeometry geometry = model.getMinecraftGeometry()[0];
        Bone[] source = geometry.getBones();
        if (source.length == 0) return null;
        Set<String> names = new HashSet<>();
        for (Bone bone : source) {
            if (bone == null || bone.getName() == null || !names.add(bone.getName())) return null;
        }
        RawGeometryTree tree = new RawGeometryTree();
        tree.properties = geometry.getProperties();
        List<Bone> pending = new ArrayList<>(Arrays.asList(source));
        Map<String, RawBoneGroup> attached = new HashMap<>();
        int index = pending.size() - 1;
        int loopsWithoutChange = 0;
        while (true) {
            if (++loopsWithoutChange > 10000) return null;
            Bone bone = pending.get(index);
            RawBoneGroup parent = attached.get(bone.getParent());
            if (!RawGeometryTree.hasParent(bone) || parent != null) {
                RawBoneGroup group = new RawBoneGroup(bone);
                if (parent == null) tree.topLevelBones.put(bone.getName(), group);
                else parent.children.put(bone.getName(), group);
                attached.put(bone.getName(), group);
                // 保持原来的逆序扫描、对象移除和附着顺序。
                pending.remove(bone);
                loopsWithoutChange = 0;
            }
            if (index == 0) {
                index = pending.size() - 1;
                if (index == -1) break;
            } else {
                index--;
            }
        }
        return tree;
    }
}
