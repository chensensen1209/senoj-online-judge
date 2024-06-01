package com.sen.senoj.judge.codesandbox;

import com.sen.senoj.judge.codesandbox.impl.ExampleCodeSandboxImpl;
import com.sen.senoj.judge.codesandbox.impl.RemoteCodeSandboxImpl;
import com.sen.senoj.judge.codesandbox.impl.ThirdPartyCodeSandboxImpl;


public class CodeSandboxFactory {
    /**
     * @param type:
     * @return CodeSandbox
     * @author xh
     * @description TODO
     * @date 2024/5/13 15:34
     */

    public static CodeSandbox newInstance(String type){
        switch (type){
            case "example":
                return new ExampleCodeSandboxImpl();
            case "remote":
                return new RemoteCodeSandboxImpl();
            case "thirdParty":
                return new ThirdPartyCodeSandboxImpl();
            default:
                return new ExampleCodeSandboxImpl();

        }
    }
}
