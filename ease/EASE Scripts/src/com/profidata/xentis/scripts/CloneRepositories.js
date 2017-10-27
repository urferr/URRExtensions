loadModule("/System/Platform");
loadModule("/System/Resources");
loadModule("/System/Git");

repoBranch = "master";
reposLocation = getWorkspace().getLocation().append("xentisjava");
reposUtil = org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();

if(!reposLocation.toFile().exists()) {
	print("Cloning git@gitserver:xentis/xentisjava"+" ...")
	clone("git@gitserver:xentis/xentisjava", reposLocation, "git", "git", repoBranch);
}
reposUtil.addConfiguredRepository(reposLocation.append(".git").toFile());

print("-- all repositories are now available --")
