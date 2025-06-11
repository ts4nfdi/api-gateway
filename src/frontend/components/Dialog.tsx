import {Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle,} from "@/components/ui/dialog";
import {Button} from "@/components/ui/button";

export default function DialogWrapper({children, isOpen, setIsOpen, onSubmit, title, showFooter = true}: any) {
    const handleSave = async () => {
        onSubmit();
        setIsOpen(false);
    };

    return (
        <Dialog open={isOpen} onOpenChange={setIsOpen}>
            <DialogContent className={`min-h-[70vh] min-w-[70vw] max-w-[800px] max-h-[1200px]`}>
                <DialogHeader>
                    <DialogTitle>{title}</DialogTitle>
                </DialogHeader>
                <div className={"overflow-y-auto h-full w-full min-h-[50vh] max-h-[1000px]"}>
                    {children}
                </div>
                {showFooter && (
                    <DialogFooter>
                        <Button variant="secondary" onClick={() => setIsOpen(false)}>Close</Button>
                        {onSubmit && <Button onClick={handleSave}>Save</Button>}
                    </DialogFooter>)
                }
            </DialogContent>
        </Dialog>
    );
}
