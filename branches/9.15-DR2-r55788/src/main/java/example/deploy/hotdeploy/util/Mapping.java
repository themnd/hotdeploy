package example.deploy.hotdeploy.util;

public interface Mapping<F, T> {
    T map(F from);
}
