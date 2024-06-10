package dev.latvian.mods.kubejs.recipe;

import dev.latvian.mods.kubejs.core.RecipeLikeKJS;
import dev.latvian.mods.rhino.Context;

@FunctionalInterface
public interface OutputReplacementTransformer {
	Object transform(RecipeLikeKJS recipe, ReplacementMatch match, OutputReplacement original, OutputReplacement with);

	record Replacement(OutputReplacement with, OutputReplacementTransformer transformer) implements OutputReplacement {
		@Override
		public String toString() {
			return with + " [transformed]";
		}

		@Override
		public Object replaceOutput(Context cx, KubeRecipe recipe, ReplacementMatch match, OutputReplacement original) {
			return transformer.transform(recipe, match, original, with);
		}
	}
}
