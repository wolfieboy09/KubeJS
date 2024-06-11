package dev.latvian.mods.kubejs.item.creativetab;

import dev.latvian.mods.kubejs.CommonProperties;
import dev.latvian.mods.kubejs.KubeJS;
import dev.latvian.mods.kubejs.item.ItemStackJS;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.util.UtilsJS;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface KubeJSCreativeTabs {
	DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KubeJS.MOD_ID);

	Supplier<CreativeModeTab> TAB = REGISTRY.register("tab", () -> CreativeModeTab.builder()
		.title(KubeJS.NAME_COMPONENT)
		.icon(() -> {
			var is = ItemStackJS.ofString(UtilsJS.BUILTIN_NBT_REGISTRY_OPS, CommonProperties.get().creativeModeTabIcon);
			return is.isEmpty() ? Items.PURPLE_DYE.getDefaultInstance() : is;
		})
		.displayItems((params, output) -> {
			for (var b : RegistryInfo.ITEM) {
				output.accept(b.get().getDefaultInstance());
			}
		})
		.build()
	);
}
